package sipserver.com.service.register;

import java.util.ArrayList;
import java.util.Set;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.header.CallID;
import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.server.auth.AccountManagerImpl;
import sipserver.com.service.Service;
import sipserver.com.service.util.GeneraterService;

public class RegisterService extends Service {

	private static StackLogger logger = CommonLogger.getLogger(RegisterService.class);

	public RegisterService() {
		super(logger, "REGISTER");
		beginTask("TRUNK", 60, null);
		ServerCore.getServerCore().addLocalExtension(new Extension("1001", "test1001", "192.168.1.100"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1002", "test1002", "192.168.1.100"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1003", "test1003", "192.168.1.100"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1004", "test1004", "192.168.1.100"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1005", "test1005", "192.168.1.100"));
	}

	@Override
	public void processRequest(RequestEvent requestEvent) throws Exception {
		String message = requestEvent.getRequest().toString();
		SipServerTransport transport = ServerCore.getTransport(requestEvent.getRequest());
		if (transport == null) {
			logger.logFatalError("Transport is null\r\n");
			throw new Exception();
		}

		ServerTransaction serverTransaction = transport.getSipProvider().getNewServerTransaction(requestEvent.getRequest());
		try {
			logger.logFatalError("RegisterRequestProcess:\r\n" + message);
			sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.TRYING);
			int code = 200;
			try {
				ContactHeader contactHeader = (ContactHeader) requestEvent.getRequest().getHeader(ContactHeader.NAME);
				if (contactHeader == null) {
					sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.UNAUTHORIZED);
					logger.logFatalError("Contact Header is Null. Message:" + message);
					return;
				}

				Extension extIncoming = new Extension(contactHeader);
				if (extIncoming == null || extIncoming.getExten() == null || extIncoming.getHost() == null) {
					sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_REQUEST);
					return;
				}

				Extension extLocal = ServerCore.getServerCore().getLocalExtension(extIncoming.getExten());
				if (extLocal == null) {
					sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.FORBIDDEN);
					return;
				}

				code = registerExtension(extIncoming, extLocal, requestEvent);
				if (code == Response.UNAUTHORIZED) {
					Response challengeResponse = transport.getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, requestEvent.getRequest());
					transport.getDigestServerAuthentication().generateChallenge(transport.getHeaderFactory(), challengeResponse, "nist.gov");
					serverTransaction.sendResponse(challengeResponse);
				}
			} catch (Exception e) {
				sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.UNAUTHORIZED);
				e.printStackTrace();
				logger.logFatalError("Message Error. Message:" + message);
				return;
			}
			sendResponseMessage(serverTransaction, requestEvent.getRequest(), code);
		} catch (Exception e) {
			e.printStackTrace();
			logger.logFatalError("Message Error. Message:" + message);
			sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT);
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent) {
		try {
			SipServerTransport transport = ServerCore.getTransport(responseEvent.getResponse());
			if (transport == null) {
				logger.logFatalError("Transport is null\r\n");
				throw new Exception();
			}
			CallIdHeader callIdHeader = (CallIdHeader) responseEvent.getResponse().getHeader(CallIdHeader.NAME);
			if (callIdHeader == null) {
				return;
			}
			String exten = getExtenFromCallIdForAcknowledge().getProperty(callIdHeader.getCallId());
			if (exten == null) {
				return;
			}
			getExtenFromCallIdForAcknowledge().remove(exten);
			Extension trunkExtension = ServerCore.getServerCore().getTrunkExtension(exten);
			if (trunkExtension == null) {
				return;
			}
			int statusCode = responseEvent.getResponse().getStatusCode();
			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				if (!isHaveAuthenticateHeader(responseEvent)) {
					logger.logFatalError("Transaction is dead ");
					throw new Exception();
				}
				AuthenticationHelper authenticationHelper = ((SipStackExt) transport.getSipStack()).getAuthenticationHelper(new AccountManagerImpl(trunkExtension), transport.getHeaderFactory());
				ClientTransaction clientTransaction = authenticationHelper.handleChallenge(responseEvent.getResponse(), responseEvent.getClientTransaction(), transport.getSipProvider(), 5, false);
				clientTransaction.sendRequest();
				getExtenFromCallIdForAcknowledge().setProperty(callIdHeader.getCallId(), trunkExtension.getExten());
				return;
			}
			if (statusCode == Response.FORBIDDEN) {
				logger.logFatalError("Forbidden " + trunkExtension.getExten());
				logger.logFatalError("Transaction is dead " + trunkExtension.getExten());
				return;
			}
			if (statusCode == Response.OK) {
				logger.logFatalError("Registered Trunk " + trunkExtension.getExten());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private int registerExtension(Extension extIncoming, Extension extLocal, RequestEvent requestEvent) throws Exception {
		if (ServerCore.getServerCore().checkRegisterationExtension(extIncoming.getExten())) {
			if (extLocal.getHost().equals(extIncoming.getHost())) {
				updateRegister(extLocal);
				return Response.OK;
			}
			unRegisterLocalExtension(extIncoming.getExten());
			return Response.UNAUTHORIZED;
		}
		if (!isHaveAuthenticateHeader(requestEvent)) {
			return Response.UNAUTHORIZED;
		}

		if (extLocal.getPass() == null) {
			return Response.FORBIDDEN;
		}

		if (!ServerCore.getTransport(requestEvent.getRequest()).getDigestServerAuthentication().doAuthenticatePlainTextPassword(requestEvent.getRequest(), extLocal.getPass())) {
			logger.logFatalError("Forbidden 2");
			return Response.FORBIDDEN;
		}
		updateRegister(extLocal);
		return Response.OK;
	}

	private void updateRegister(Extension extLocal) {
		extLocal.setRegister(true);
		beginTask(extLocal.getExten(), extLocal.getExpiresTime(), extLocal.getExten());
	}

	public void unRegisterLocalExtension(String exten) {
		Extension extension = ServerCore.getServerCore().getLocalExtension(exten);
		if (extension == null) {
			return;
		}
		extension.setRegister(false);
	}

	public void unRegisterTrunkExtension(String exten) {
		Extension extension = ServerCore.getServerCore().getTrunkExtension(exten);
		if (extension == null) {
			return;
		}
		extension.setRegister(false);
	}

	@Override
	public void beginTask(String taskId, int timeout, String exten) {
		ServerCore.getServerCore().getTimerService().registerTask(taskId + RegisterService.getNAME(), timeout);
		getGeneralTransaction().setProperty(taskId, exten);
	}

	@Override
	public void endTask(String taskId) {
		if (taskId.contains("TRUNK")) {
			processTrunkRegister(taskId);
			return;
		}
		String exten = getGeneralTransaction().getProperty(taskId);
		if (exten == null) {
			return;
		}
		Extension extension = ServerCore.getServerCore().getLocalExtension(exten);
		if (extension == null) {
			return;
		}
		extension.setRegister(false);

	}

	private void processTrunkRegister(String taskId) {
		if (taskId.contains("BGN")) {
			String exten = getGeneralTransaction().getProperty(taskId);
			if (exten == null) {
				return;
			}
			String callId = getCallIdFromTask().getProperty(taskId);
			if (callId == null) {
				return;
			}
			// For ACKNOWLEDGE
			if (getExtenFromCallIdForAcknowledge().getProperty(callId) == null) {
				return;
			}
			sendFirstRegisterTrunk(ServerCore.getServerCore().getTrunkExtension(exten));
			return;
		}

		Set<Object> keys = ServerCore.getServerCore().getTrunkExtensionList().keySet();
		for (Object key : keys) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					sendFirstRegisterTrunk(ServerCore.getServerCore().getTrunkExtension((String) key));
				}
			}).start();
		}
	}

	private void sendFirstRegisterTrunk(Extension extension) {
		try {
			Request requestMessage = createRegisterMessage(extension);
			SipServerTransport transport = ServerCore.getTransport(requestMessage);
			if (transport == null) {
				getLogger().logFatalError("Transport is null, ssasfddgs");
				throw new Exception();
			}
			ClientTransaction clientTransaction = transport.getSipProvider().getNewClientTransaction(requestMessage);
			clientTransaction.sendRequest();
			String taskId = extension.getExten() + "TRUNKBGN";
			String callId = ((CallIdHeader) requestMessage.getHeader(CallIdHeader.NAME)).getCallId();
			getCallIdFromTask().setProperty(taskId, callId);
			getExtenFromCallIdForAcknowledge().setProperty(callId, extension.getExten());
			beginTask(taskId, 2, extension.getExten());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Request createRegisterMessage(Extension extension) {
		try {
			SipServerTransport transport = ServerCore.getTransport(extension.getTransportType());
			if (transport == null) {
				throw new Exception();
			}

			SipURI fromAddress = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());

			Address fromNameAddress = transport.getAddressFactory().createAddress(fromAddress);
			if (extension.getDisplayName() != null) {
				fromNameAddress.setDisplayName(extension.getDisplayName());
			}

			FromHeader fromHeader = transport.getHeaderFactory().createFromHeader(fromNameAddress, GeneraterService.getUUidForTag());
			// create To Header
			SipURI toAddress = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			Address toNameAddress = transport.getAddressFactory().createAddress(toAddress);
			if (extension.getDisplayName() != null) {
				toNameAddress.setDisplayName(extension.getDisplayName());
			}
			ToHeader toHeader = transport.getHeaderFactory().createToHeader(toNameAddress, null);

			// create Request URI
			String peerHostPort = extension.getHost() + ":" + extension.getPort();
			SipURI requestURI = transport.getAddressFactory().createSipURI(extension.getExten(), peerHostPort);

			// Create ViaHeaders

			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			ViaHeader viaHeader = transport.getHeaderFactory().createViaHeader(transport.getHost(), transport.getPort(), transport.getProtocol(), GeneraterService.getUUidForBranch());
			// add via headers
			viaHeaders.add(viaHeader);

			SipURI sipuri = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			sipuri.setPort(extension.getPort());
			sipuri.setLrParam();

			RouteHeader routeHeader = transport.getHeaderFactory().createRouteHeader(transport.getAddressFactory().createAddress(sipuri));

			// Create ContentTypeHeader
			// ContentTypeHeader contentTypeHeader =
			// ProtocolObjects.headerFactory.createContentTypeHeader("application",
			// "sdp");

			// Create a new CallId header
			// CallIdHeader callIdHeader = sipProvider.getNewCallId();
			CallIdHeader callIdHeader = new CallID(GeneraterService.getUUid(10));

			// Create a new Cseq header
			CSeqHeader cSeqHeader = transport.getHeaderFactory().createCSeqHeader(1L, Request.REGISTER);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = transport.getHeaderFactory().createMaxForwardsHeader(70);

			// Create the request.
			Request request = transport.getMessageFactory().createRequest(requestURI, Request.REGISTER, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			// Create contact headers

			SipURI contactUrl = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			contactUrl.setPort(transport.getPort());

			// Create the contact name address.
			SipURI contactURI = transport.getAddressFactory().createSipURI(extension.getExten(), transport.getHost());
			contactURI.setPort(transport.getPort());

			Address contactAddress = transport.getAddressFactory().createAddress(contactURI);

			if (extension.getDisplayName() != null) {
				// Add the contact address.
				contactAddress.setDisplayName(extension.getDisplayName());
			}

			ContactHeader contactHeader = transport.getHeaderFactory().createContactHeader(contactAddress);
			request.addHeader(contactHeader);
			// Dont use the Outbound Proxy. Use Lr instead.
			request.setHeader(routeHeader);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
