package sipserver.com.service.invite;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.header.CallID;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;

import java.util.ArrayList;
import java.util.Set;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
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

public class InviteServiceOut extends Service {

	private static StackLogger logger = CommonLogger.getLogger(InviteServiceOut.class);

	public InviteServiceOut() {
		super(logger);
		beginTask("", 60, null);
		ServerCore.getServerCore().addTrunkExtension(new Extension("9001", "test9001", "192.168.1.108"));
		processTrunkRegister();
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport) throws Exception {
		// NON
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		try {
			if (responseEvent.getClientTransaction() == null) {
				throw new Exception();
			}

			if (responseEvent.getClientTransaction().getRequest() == null) {
				throw new Exception();
			}

			ContactHeader requContactHeader = (ContactHeader) responseEvent.getClientTransaction().getRequest().getHeader(ContactHeader.NAME);

			if (requContactHeader == null) {
				throw new Exception();
			}

			Extension trunkExtension = new Extension(requContactHeader);
			int statusCode = responseEvent.getResponse().getStatusCode();
			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				if (!isHaveAuthenticateHeader(responseEvent)) {
					logger.logFatalError("Transaction is dead ");
					throw new Exception();
				}
				AuthenticationHelper authenticationHelper = ((SipStackExt) transport.getSipStack()).getAuthenticationHelper(new AccountManagerImpl(trunkExtension), transport.getHeaderFactory());
				ClientTransaction clientTransaction = authenticationHelper.handleChallenge(responseEvent.getResponse(), responseEvent.getClientTransaction(), transport.getSipProvider(), 5, false);
				ServerCore.getServerCore().getTransportService().sendRequestMessage(clientTransaction);
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

	@Override
	public void beginTask(String taskId, int timeout, Object exten) {
		ServerCore.getServerCore().getTimerService().registerTask(taskId + InviteServiceOut.class.getName(), timeout);
	}

	@Override
	public void endTask(String taskId) {
		processTrunkRegister();
		logger.logFatalError("Process UnRegister");
	}

	public void unRegisterTrunkExtension(String exten) {
		Extension extension = ServerCore.getServerCore().getTrunkExtension(exten);
		if (extension == null) {
			return;
		}
		extension.setRegister(false);
	}

	private void processTrunkRegister() {
		Set<Object> keys = ServerCore.getServerCore().getTrunkExtensionList().keySet();
		for (Object key : keys) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Extension extTrunk = ServerCore.getServerCore().getTrunkExtension((String) key);
						if (extTrunk == null) {
							return;
						}
						Request requestMessage = createRegisterMessage(extTrunk);
						SipServerTransport transport = ServerCore.getTransport(requestMessage);
						if (transport == null) {
							getLogger().logFatalError("Transport is null, ssasfddgs");
							throw new Exception();
						}
						ServerCore.getServerCore().getTransportService().sendRequestMessage(transport.getSipProvider().getNewClientTransaction(requestMessage));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

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

	@Override
	public void mediaServerEvents(JainMgcpResponseEvent jainmgcpresponseevent, String callID) {
		// TODO Auto-generated method stub
		
	}

}
