package sipserver.com.executer;

import java.util.Properties;

import javax.sip.RequestEvent;
import javax.sip.header.CallIdHeader;
import sipserver.com.server.SipServer;

public class TransactionManager {

	private Properties transactions = new Properties();
	private SipServer sipServer;

	public TransactionManager(SipServer sipServer) {
		this.sipServer = sipServer;
	}

	public Transaction getTransaction(String callId) {
		return (Transaction) transactions.get(callId);
	}

	public Transaction addTransaction(RequestEvent requestEvent) {
		try {
			String callId = ((CallIdHeader) requestEvent.getRequest().getHeader("Call-ID")).getCallId();
			Transaction transaction = new Transaction(sipServer, callId);
			transactions.put(callId, transaction);
			System.out.println("Adding Transaction callId:" + callId);
			return transaction;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
