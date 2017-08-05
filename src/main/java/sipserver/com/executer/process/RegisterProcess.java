package sipserver.com.executer.process;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.ContactHeader;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;
import sipserver.com.server.SipServer;

public class RegisterProcess extends ProcessMessage {
	
	private static StackLogger logger = CommonLogger.getLogger(RegisterProcess.class);

	public RegisterProcess(SipServer sipServer) throws Exception {
		super(sipServer);
	}

	@Override
	public void processRequest(RequestEvent requestEvent) {
		ServerTransaction serverTransaction = null;
		String message = requestEvent.getRequest().toString();
		try {
			logger.logFatalError("RegisterRequestProcess:\r\n" + message);
			serverTransaction = getSipServer().getProvider().getNewServerTransaction(requestEvent.getRequest());
			sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.TRYING);
			int code = 200;
			try {
				ContactHeader contactHeader = (ContactHeader) requestEvent.getRequest().getHeader(ContactHeader.NAME);
				if (contactHeader == null) {
					sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.UNAUTHORIZED);
					logger.logFatalError("Contact Header is Null. Message:" + message);
					return;
				}
				//TODO: must be simple
				Extension extension = new Extension(contactHeader);
				code = getSipServer().getServiceProvider().getResgisterService().register(extension, getDigestServerAuthentication(), requestEvent.getRequest());
				if (code == Response.UNAUTHORIZED) {
					Response challengeResponse = getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, requestEvent.getRequest());
					getDigestServerAuthentication().generateChallenge(getHeaderFactory(), challengeResponse, "nist.gov");
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
		logger.logFatalError("RegisterResponseProcess:\r\n" + responseEvent.getResponse().toString());
	}

}
