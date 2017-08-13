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
		Transaction transaction = getSipServer().getTransactionManager().addTransactionIn(callId, requestEvent.getRequest().getMethod());
		if (transaction == null) {
			// return no transaction found
			return;
		}
		transaction.processRequestTransaction(requestEvent);
	}

	public void addResponseMessage(ResponseEvent responseEvent) {
		String callId = ((CallIdHeader) responseEvent.getResponse().getHeader("Call-ID")).getCallId();
		Transaction transaction = getSipServer().getTransactionManager().addTransactionIn(callId, null);
		if (transaction == null) {
			logger.logFatalError("Transaction Not Found");
			return;
		}
		transaction.processResponseTransaction(responseEvent);
	}

	public SipServer getSipServer() {
		return sipServer;
	}

}
