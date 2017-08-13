package sipserver.com.executer;

import java.util.Properties;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.message.Request;
import sipserver.com.domain.Extension;
import sipserver.com.server.SipServer;

public class TransactionManager {

	private Properties transactions = new Properties();
	private SipServer sipServer;

	private static StackLogger logger = CommonLogger.getLogger(TransactionManager.class);

	public TransactionManager(SipServer sipServer) {
		this.sipServer = sipServer;
	}

	private Transaction getTransaction(String callId) {
		return (Transaction) transactions.get(callId);
	}

	public Transaction addTransactionIn(String callId, String method) {
		try {
			Transaction transaction = getTransaction(callId);
			if (transaction != null) {
				return transaction;
			}
			if (method == null) {
				return null;
			}
			if (method.equals(Request.REGISTER)) {
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
	
	public Transaction addTransactionOut(String callId, String method, Extension extension) {
		try {
			Transaction transaction = getTransaction(callId);
			if (transaction != null) {
				return transaction;
			}
			if (method == null) {
				return null;
			}
			if (method.equals(Request.REGISTER)) {
				RegisterTransactionOut registerTransactionOut = new RegisterTransactionOut(sipServer, callId, extension);
				transactions.put(callId, registerTransactionOut);
				return registerTransactionOut;
			}
			logger.logFatalError("Adding Transaction callId:" + callId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
