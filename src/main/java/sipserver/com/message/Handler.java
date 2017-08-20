package sipserver.com.message;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.message.Request;
import sipserver.com.executer.core.ServerCore;

public class Handler {

	private static StackLogger logger = CommonLogger.getLogger(Handler.class);

	public static void addRequestMessage(RequestEvent requestEvent) {
	}

	public void addResponseMessage(ResponseEvent responseEvent) {
		// String callId = ((CallIdHeader)
		// responseEvent.getResponse().getHeader("Call-ID")).getCallId();
		// Transaction transaction =
		// getSipServer().getTransactionManager().addTransactionIn(callId,
		// null);
		// if (transaction == null) {
		// logger.logFatalError("Transaction Not Found");
		// return;
		// }
		// transaction.processResponseTransaction(responseEvent);
	}

	public void createRequestProcess(RequestEvent requestEvent) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (requestEvent.getRequest().getMethod().equals(Request.REGISTER)) {
						ServerCore.getServerCore().getRegisterService().processRequest(requestEvent);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void createResponseProcess(ResponseEvent responseEvent) {
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// if (requestEvent.getRequest().getMethod().equals(Request.REGISTER)) {
		// ServerCore.getServerCore().getRegisterService().processRequest(requestEvent);
		// return;
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }).start();
		//
	}

}
