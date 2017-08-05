package sipserver.com.executer;

import java.util.Properties;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.RequestEvent;
import javax.sip.header.CallIdHeader;
import sipserver.com.server.SipServer;

public class TransactionManager {

	private Properties transactions = new Properties();
	private SipServer sipServer;
	
	private static StackLogger logger = CommonLogger.getLogger(TransactionManager.class);

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
			logger.logFatalError("Adding Transaction callId:" + callId);
			return transaction;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
