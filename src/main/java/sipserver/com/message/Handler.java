package sipserver.com.message;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.header.CallIdHeader;
import sipserver.com.executer.Transaction;
import sipserver.com.server.SipServer;

public class Handler {

	private SipServer sipServer;

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
			System.out.println("Transaction Not Found");
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
