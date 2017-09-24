package sipserver.com.service.bridge;

import javax.sip.ServerTransaction;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Response;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.util.ExceptionService;

public class StatusService {

	private void sendBridgeResponse(CallParam toCallParam, int statusCode, String sdpContent) {
		CallParam bridgeCallParam = toCallParam.getBridgeCallParam();
		if (bridgeCallParam.getTransaction() instanceof ServerTransaction) {
			ServerCore.getServerCore().getTransportService().sendResponseMessage((ServerTransaction) bridgeCallParam.getTransaction(), bridgeCallParam.getRequest(), statusCode, sdpContent);
		}
	}

	public void ringing(CallParam errorCallParam) {
		sendBridgeResponse(errorCallParam, Response.RINGING, null);
	}

	public void error(CallParam errorCallParam) {
		sendBridgeResponse(errorCallParam, Response.SERVER_INTERNAL_ERROR, null);
	}

	public void noAnswer(CallParam noAnswerCallParam) {
		sendBridgeResponse(noAnswerCallParam, Response.DECLINE, null);
	}

	public void busy(CallParam toCallParam) {
		sendBridgeResponse(toCallParam, Response.BUSY_HERE, null);

	}

	public void declined(CallParam toCallParam) {
		sendBridgeResponse(toCallParam, Response.DECLINE, null);

	}

	public void ok(CallParam toCallParam, String sdpRemoteContent) {
		sendBridgeResponse(toCallParam, Response.OK, sdpRemoteContent);
		String fromCallID = ((CallIdHeader) toCallParam.getBridgeCallParam().getRequest().getHeader(CallIdHeader.NAME)).getCallId();
		String toCallID = ((CallIdHeader) toCallParam.getRequest().getHeader(CallIdHeader.NAME)).getCallId();
		ServerCore.getServerCore().putChannel(fromCallID, toCallParam.getBridgeCallParam());
		ServerCore.getServerCore().putChannel(toCallID, toCallParam);
	}

	public void cancel(CallParam fromCallParam) {
		try {
			ExceptionService.checkNullObject(fromCallParam.getBridgeCallParam());
			SipServerTransport transport = ServerCore.getTransport(fromCallParam.getBridgeCallParam().getRequest());
			ExceptionService.checkNullObject(transport);
			ServerCore.getServerCore().getInviteServiceOut().beginCancelFlow(fromCallParam.getBridgeCallParam(), transport);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void bye(CallParam callParam) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
