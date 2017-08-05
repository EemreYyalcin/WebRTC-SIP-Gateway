package sipserver.com.message;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.header.CallIdHeader;
import sipserver.com.executer.Transaction;
import sipserver.com.server.SipServer;

public class Handler {

	private SipServer sipServer;
	
	private static StackLogger logger = CommonLogger.getLogger(Handler.class);

	public Handler(SipServer sipServer) {
		this.sipServer = sipServer;
	}

	public void addRequestMessage(RequestEvent requestEvent) {
		String callId = ((CallIdHeader) requestEvent.getRequest().getHeader("Call-ID")).getCallId();
		Transaction transaction = getSipServer().getTransactionManager().getTransaction(callId);
		if (transaction == null) {
			transaction = getSipServer().getTransactionManager().addTransaction(requestEvent);
		}
		if (transaction == null) {
			// return no transaction found
			return;
		}
		if (transaction.processRequestMessage(requestEvent)) {
			transaction.process();
		}
	}

	public void addResponseMessage(ResponseEvent responseEvent) {
		String callId = ((CallIdHeader) responseEvent.getResponse().getHeader("Call-ID")).getCallId();
		Transaction transaction = getSipServer().getTransactionManager().getTransaction(callId);
		if (transaction == null) {
			logger.logFatalError("Transaction Not Found");
			// return no transaction found
			return;
		}
		if (transaction.processResponseMessage(responseEvent)) {
			transaction.process();
		}
	}

	public SipServer getSipServer() {
		return sipServer;
	}

}
