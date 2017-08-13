package sipserver.com.executer;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
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
	public void processRequest(RequestEvent requestEvent) {
		String message = requestEvent.getRequest().toString();
		try {
			logger.logFatalError("RegisterRequestProcess:\r\n" + message);
			setServerTransaction(getSipServer().getSipProvider().getNewServerTransaction(requestEvent.getRequest()));

			sendResponseMessage(getServerTransaction(), requestEvent.getRequest(), Response.TRYING);
			int code = 200;
			try {
				ContactHeader contactHeader = (ContactHeader) requestEvent.getRequest().getHeader(ContactHeader.NAME);
				if (contactHeader == null) {
					sendResponseMessage(getServerTransaction(), requestEvent.getRequest(), Response.UNAUTHORIZED);
					logger.logFatalError("Contact Header is Null. Message:" + message);
					return;
				}
				Extension extension = new Extension(contactHeader);
				code = getSipServer().getRegisterService().register(extension, requestEvent.getRequest());
				if (code == Response.UNAUTHORIZED) {
					Response challengeResponse = getSipServer().getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, requestEvent.getRequest());
					getSipServer().getDigestServerAuthentication().generateChallenge(getSipServer().getHeaderFactory(), challengeResponse, "nist.gov");
					getServerTransaction().sendResponse(challengeResponse);
				}
			} catch (Exception e) {
				sendResponseMessage(getServerTransaction(), requestEvent.getRequest(), Response.UNAUTHORIZED);
				e.printStackTrace();
				logger.logFatalError("Message Error. Message:" + message);
				return;
			}
			sendResponseMessage(getServerTransaction(), requestEvent.getRequest(), code);
		} catch (Exception e) {
			e.printStackTrace();
			logger.logFatalError("Message Error. Message:" + message);
			sendResponseMessage(getServerTransaction(), requestEvent.getRequest(), Response.BAD_EVENT);
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent) {
		logger.logFatalError("RegisterResponseProcess:\r\n" + responseEvent.getResponse().toString());
	}

	@Override
	public void processRequestTransaction(RequestEvent requestEvent) {
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
	}

	@Override
	public void processResponseTransaction(ResponseEvent responseEvent) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					processResponse(responseEvent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

}
