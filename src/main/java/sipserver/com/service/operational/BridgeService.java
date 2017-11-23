package sipserver.com.service.operational;

import javax.sip.message.Response;

import com.noyan.util.NullUtil;

import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.executer.sip.transaction.Transaction;

public class BridgeService {

	private static void sendBridgeResponse(Transaction transaction, int statusCode) {
		sendBridgeResponse(transaction, statusCode, null);
	}

	private static void sendBridgeResponse(Transaction transaction, int statusCode, String sdp) {

		if (NullUtil.isNull(transaction)) {
			return;
		}
		if (transaction instanceof ServerTransaction) {
			((ServerTransaction) transaction).sendResponseMessage(statusCode, sdp);
		}

		if (NullUtil.isNotNull(transaction.getBridgeTransaction())) {
			if (transaction.getBridgeTransaction() instanceof ServerTransaction) {
				((ServerTransaction) transaction.getBridgeTransaction()).sendResponseMessage(statusCode, sdp);
			}
		}
	}

	public static void ringing(Transaction transaction) {
		sendBridgeResponse(transaction, Response.RINGING);
	}

	public static void error(Transaction transaction) {
		sendBridgeResponse(transaction, Response.SERVER_INTERNAL_ERROR);
	}

	public static void noAnswer(Transaction transaction) {
		sendBridgeResponse(transaction, Response.DECLINE);
	}

	public static void busy(Transaction transaction) {
		sendBridgeResponse(transaction, Response.BUSY_HERE);
	}

	public static void declined(Transaction transaction) {
		sendBridgeResponse(transaction, Response.DECLINE);
	}

	public static void noRoute(Transaction transaction) {
		sendBridgeResponse(transaction, Response.BUSY_HERE);
	}

	public static void ok(Transaction transaction, String sdp) {
		sendBridgeResponse(transaction, Response.OK, sdp);
	}

	public static void cancel(ClientTransaction clientTransaction) {
		clientTransaction.sendCancelMessage();
	}

	public static void bye(Transaction transaction) {
		// transaction.sendByeMessage();
		if (NullUtil.isNull(transaction.getBridgeTransaction())) {
			return;
		}
		transaction.getBridgeTransaction().sendByeMessage();

	}

}
