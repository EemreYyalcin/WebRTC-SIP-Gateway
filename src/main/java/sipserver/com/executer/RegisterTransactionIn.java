package sipserver.com.executer;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.ServerTransaction;
import javax.sip.header.ContactHeader;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;
import sipserver.com.server.SipServer;

public class RegisterTransactionIn extends Transaction {

	private static StackLogger logger = CommonLogger.getLogger(RegisterTransactionIn.class);

	public RegisterTransactionIn(SipServer sipServer, String callId) {
		super(sipServer, callId);
	}

	@Override
	public void processRequest() {
		ServerTransaction serverTransaction = null;
		String message = getRequestEvent().getRequest().toString();
		try {
			logger.logFatalError("RegisterRequestProcess:\r\n" + message);
			serverTransaction = getSipServer().getProvider().getNewServerTransaction(getRequestEvent().getRequest());
			sendResponseMessage(serverTransaction, getRequestEvent().getRequest(), Response.TRYING);
			int code = 200;
			try {
				ContactHeader contactHeader = (ContactHeader) getRequestEvent().getRequest().getHeader(ContactHeader.NAME);
				if (contactHeader == null) {
					sendResponseMessage(serverTransaction, getRequestEvent().getRequest(), Response.UNAUTHORIZED);
					logger.logFatalError("Contact Header is Null. Message:" + message);
					return;
				}
				//TODO: must be simple
				Extension extension = new Extension(contactHeader);
				code = getSipServer().getServiceProvider().getResgisterService().register(extension, getDigestServerAuthentication(), getRequestEvent().getRequest());
				if (code == Response.UNAUTHORIZED) {
					Response challengeResponse = getSipServer().getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, getRequestEvent().getRequest());
					getDigestServerAuthentication().generateChallenge(getSipServer().getHeaderFactory(), challengeResponse, "nist.gov");
					serverTransaction.sendResponse(challengeResponse);
				}
			} catch (Exception e) {
				sendResponseMessage(serverTransaction, getRequestEvent().getRequest(), Response.UNAUTHORIZED);
				e.printStackTrace();
				logger.logFatalError("Message Error. Message:" + message);
				return;
			}
			sendResponseMessage(serverTransaction, getRequestEvent().getRequest(), code);
		} catch (Exception e) {
			e.printStackTrace();
			logger.logFatalError("Message Error. Message:" + message);
			sendResponseMessage(serverTransaction, getRequestEvent().getRequest(), Response.BAD_EVENT);
		}
	}

	@Override
	public void processResponse() {
		logger.logFatalError("RegisterResponseProcess:\r\n" + getResponseEvent().getResponse().toString());
	}


}
