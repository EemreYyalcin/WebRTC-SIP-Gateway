package sipserver.com.service.operational;

import javax.sip.ServerTransaction;
import javax.sip.message.Response;

import sipserver.com.parameter.param.CallParam;

public class BridgeService {

	private static void sendBridgeResponse(CallParam toCallParam, int statusCode, String sdpContent) {
		CallParam bridgeCallParam = toCallParam.getBridgeCallParam();
		if (bridgeCallParam.getTransaction() instanceof ServerTransaction) {
			// ServerCore.getServerCore().getTransportService().sendResponseMessage((ServerTransaction)
			// bridgeCallParam.getTransaction(), bridgeCallParam.getRequest(), statusCode,
			// sdpContent);
		}
	}

	public static void ringing(CallParam errorCallParam) {
		sendBridgeResponse(errorCallParam, Response.RINGING, null);
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
		if (fromCallParam.getTransaction() instanceof ServerTransaction) {
			// ServerCore.getServerCore().getTransportService().sendResponseMessage((ServerTransaction)
			// fromCallParam.getTransaction(), fromCallParam.getRequest(),
			// Response.BUSY_HERE, null);
		}
	}

	public static void ok(CallParam toCallParam) {
		sendBridgeResponse(toCallParam, Response.OK, toCallParam.getSdpRemoteContent());
	}

	public static void cancel(CallParam fromCallParam) {
		try {
			// ServerCore.getServerCore().getTransportService().sendResponseMessage((ServerTransaction)
			// fromCallParam.getTransaction(), fromCallParam.getSecondrequest(),
			// Response.OK, null);
			// ServerCore.getServerCore().getInviteServiceOut().beginCancelFlow(fromCallParam.getBridgeCallParam(),
			// transport);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void bye(CallParam callParam) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
