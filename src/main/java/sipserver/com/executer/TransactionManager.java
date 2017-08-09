package sipserver.com.executer;

import java.util.Properties;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.RequestEvent;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
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
			if (requestEvent.getRequest().getMethod().equals(Request.REGISTER)) {
				RegisterTransactionIn registerTransaction = new RegisterTransactionIn(sipServer, callId);
				transactions.put(callId, registerTransaction);
				return registerTransaction;
			}
			logger.logFatalError("Adding Transaction callId:" + callId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
