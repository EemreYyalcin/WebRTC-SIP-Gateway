package sipserver.com.service.operational;

import java.util.Objects;

import javax.sip.message.Response;

import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.parameter.param.CallParam;

public class BridgeService {

	private static void sendBridgeResponse(CallParam fromCallParam, int statusCode, String sdpContent) {
		if (Objects.isNull(fromCallParam)) {
			return;
		}
		CallParam bridgeCallParam = fromCallParam.getBridgeCallParam();
		if (Objects.isNull(bridgeCallParam)) {
			return;
		}
		if (bridgeCallParam.getTransaction() instanceof ServerTransaction) {
			ServerTransaction serverTransaction = (ServerTransaction) bridgeCallParam.getTransaction();
			serverTransaction.sendResponseMessage(statusCode, sdpContent);
		}
	}

	public static void ringing(CallParam fromCallParam) {
		sendBridgeResponse(fromCallParam, Response.RINGING, null);
	}

	public static void error(CallParam errorCallParam) {
		sendBridgeResponse(errorCallParam, Response.SERVER_INTERNAL_ERROR, null);
	}

	public static void noAnswer(CallParam noAnswerCallParam) {
		sendBridgeResponse(noAnswerCallParam, Response.DECLINE, null);
	}

	public static void busy(CallParam toCallParam) {
		sendBridgeResponse(toCallParam, Response.BUSY_HERE, null);
	}

	public static void declined(CallParam toCallParam) {
		sendBridgeResponse(toCallParam, Response.DECLINE, null);
	}

	public static void noRoute(CallParam fromCallParam) {
		sendBridgeResponse(fromCallParam, Response.BUSY_HERE, null);
	}

	public static void ok(CallParam toCallParam, Response response) {
		sendBridgeResponse(toCallParam, Response.OK, toCallParam.getSdpRemoteContent());
	}

	public static void cancel(CallParam fromCallParam) {
		if (Objects.isNull(fromCallParam.getBridgeCallParam())) {
			return;
		}
		ClientTransaction clientTransaction = (ClientTransaction) fromCallParam.getBridgeCallParam().getTransaction();
		Objects.requireNonNull(clientTransaction);
		clientTransaction.sendCancelMessage();
	}

	public static void bye(CallParam fromCallParam) {
		if (Objects.isNull(fromCallParam.getBridgeCallParam())) {
			return;
		}
		fromCallParam.getBridgeCallParam().getTransaction().sendByeMessage();
	}

}
