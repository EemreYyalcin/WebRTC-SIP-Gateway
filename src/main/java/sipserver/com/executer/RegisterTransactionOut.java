package sipserver.com.executer;

import java.util.ArrayList;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.header.CallID;
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
import sipserver.com.server.SipServer;
import sipserver.com.server.auth.AccountManagerImpl;
import sipserver.com.service.util.GeneraterService;
import sipserver.com.service.util.SipMessageService;
import sipserver.com.timer.control.RegisterTrunkControl;

public class RegisterTransactionOut extends Transaction {

	private Extension extension;

	private static StackLogger logger = CommonLogger.getLogger(RegisterTransactionOut.class);

	private boolean acknowledge = false;

	public RegisterTransactionOut(SipServer sipServer, String callId, Extension extension) {
		super(sipServer, callId);
		setExtension(extension);
	}

	@Override
	protected void processRequest(RequestEvent requestEvent) {
		try {
			if (getRequest() == null) {
				setRequest());
			}
			if (isAcknowledge()) {
				return;
			}
			// Create the client transaction.
			setClientTransaction(getSipServer().getSipProvider().getNewClientTransaction(getRequest()));
			// send the request out.
			setAcknowledge(false);
			getClientTransaction().sendRequest();
			setSipDialog(getClientTransaction().getDialog());
			getSipServer().getSipServerTimer().registerTask(new RegisterTrunkControl(2, getCallId(), this));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void processResponse(ResponseEvent responseEvent) {
		try {
			int statusCode = responseEvent.getResponse().getStatusCode();
			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				if (!SipMessageService.isHaveAuthenticateHeader(responseEvent.getResponse())) {
					logger.logFatalError("Transaction is dead " + getExtension().getExten());
					return;
				}
				AuthenticationHelper authenticationHelper = ((SipStackExt) getSipServer().getSipStack()).getAuthenticationHelper(new AccountManagerImpl(getExtension()), getSipServer().getHeaderFactory());
				setClientTransaction(authenticationHelper.handleChallenge(responseEvent.getResponse(), getClientTransaction(), getSipServer().getSipProvider(), 5, false));
				getClientTransaction().sendRequest();
				return;
			}
			if (statusCode == Response.FORBIDDEN) {
				logger.logFatalError("Forbidden " + getExtension().getExten());
				logger.logFatalError("Transaction is dead " + getExtension().getExten());
				return;
			}
			if (statusCode == Response.OK) {
				logger.logFatalError("Registered Trunk " + getExtension().getExten());

				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void processRequestTransaction(RequestEvent requestEvent) {
		try {
			if (requestEvent != null) {
				logger.logFatalError("Error Request Event ");
				throw new Exception();
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						processRequest(requestEvent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Override
	public void processResponseTransaction(ResponseEvent responseEvent) {
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						setAcknowledge(true);
						processResponse(responseEvent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Request createRegisterMessage(Extension extension, String callId) {
		try {
			SipURI fromAddress = getSipServer().getAddressFactory().createSipURI(extension.getExten(), extension.getHost());

			Address fromNameAddress = getSipServer().getAddressFactory().createAddress(fromAddress);
			if (extension.getDisplayName() != null) {
				fromNameAddress.setDisplayName(extension.getDisplayName());
			}

			FromHeader fromHeader = getSipServer().getHeaderFactory().createFromHeader(fromNameAddress, GeneraterService.getUUidForTag());

			// create To Header
			SipURI toAddress = getSipServer().getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			Address toNameAddress = getSipServer().getAddressFactory().createAddress(toAddress);
			if (extension.getDisplayName() != null) {
				toNameAddress.setDisplayName(extension.getDisplayName());
			}
			ToHeader toHeader = getSipServer().getHeaderFactory().createToHeader(toNameAddress, null);

			// create Request URI
			String peerHostPort = extension.getHost() + ":" + extension.getPort();
			SipURI requestURI = getSipServer().getAddressFactory().createSipURI(extension.getExten(), peerHostPort);

			// Create ViaHeaders

			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			ViaHeader viaHeader = getSipServer().getHeaderFactory().createViaHeader(getSipServer().getHost(), getSipServer().getPort(), getSipServer().getProtocol(), GeneraterService.getUUidForBranch());
			// add via headers
			viaHeaders.add(viaHeader);

			SipURI sipuri = getSipServer().getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			sipuri.setPort(extension.getPort());
			sipuri.setLrParam();

			RouteHeader routeHeader = getSipServer().getHeaderFactory().createRouteHeader(getSipServer().getAddressFactory().createAddress(sipuri));

			// Create ContentTypeHeader
			// ContentTypeHeader contentTypeHeader =
			// ProtocolObjects.headerFactory.createContentTypeHeader("application",
			// "sdp");

			// Create a new CallId header
			// CallIdHeader callIdHeader = sipProvider.getNewCallId();
			CallIdHeader callIdHeader = new CallID(callId);

			// Create a new Cseq header
			CSeqHeader cSeqHeader = getSipServer().getHeaderFactory().createCSeqHeader(1L, Request.REGISTER);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = getSipServer().getHeaderFactory().createMaxForwardsHeader(70);

			// Create the request.
			Request request = getSipServer().getMessageFactory().createRequest(requestURI, Request.REGISTER, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			// Create contact headers

			SipURI contactUrl = getSipServer().getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			contactUrl.setPort(getSipServer().getPort());

			// Create the contact name address.
			SipURI contactURI = getSipServer().getAddressFactory().createSipURI(extension.getExten(), getSipServer().getHost());
			contactURI.setPort(getSipServer().getPort());

			Address contactAddress = getSipServer().getAddressFactory().createAddress(contactURI);

			if (extension.getDisplayName() != null) {
				// Add the contact address.
				contactAddress.setDisplayName(extension.getDisplayName());
			}

			ContactHeader contactHeader = getSipServer().getHeaderFactory().createContactHeader(contactAddress);
			request.addHeader(contactHeader);
			// Dont use the Outbound Proxy. Use Lr instead.
			request.setHeader(routeHeader);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	public boolean isAcknowledge() {
		return acknowledge;
	}

	public void setAcknowledge(boolean acknowledge) {
		this.acknowledge = acknowledge;
	}

}
