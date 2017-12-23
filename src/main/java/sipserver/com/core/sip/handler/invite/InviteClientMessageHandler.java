package sipserver.com.core.sip.handler.invite;

import javax.sip.message.Request;
import javax.websocket.Session;

import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.sip.parameter.constant.Constant.MessageState;
import sipserver.com.core.sip.service.RouteService;
import sipserver.com.domain.Extension;

public class InviteClientMessageHandler extends MessageHandler {

	public InviteClientMessageHandler(Request request, String remoteAddress, int remotePort) {
		super(request, remoteAddress, remotePort);
	}

	public InviteClientMessageHandler(Request request, Session session) {
		super(request, session);
	}

	public InviteClientMessageHandler(Request request, Extension extension) {
		super(request, extension);
	}

	@Override
	public boolean onTrying() {
		messageState = MessageState.TRYING;
		sendMessage(getRequest());
		return true;
	}

	@Override
	public boolean onReject(int statusCode) {
		RouteService.observeBridgingForReject(getCallParam(), statusCode);
		messageState = MessageState.BUSY;
		onFinish();
		return false;
	}

	@Override
	public boolean onOk(String content) {
		if (messageState == MessageState.BYE) {
			RouteService.observeBridgingForBye(getCallParam());
			sendACK();
			onFinish();
			return true;
		}
		if (!super.onOk(content)) {
			messageState = MessageState.FAIL;
			return false;
		}
		RouteService.observeBridgingForAcceptCall(getCallParam(), content);
		messageState = MessageState.CALLING;
		sendACK();
		return true;
	}

	@Override
	public boolean onFinish() {
		messageState = MessageState.FINISH;
		return false;
	}

	@Override
	public boolean onRinging() {
		if (messageState != MessageState.TRYING && messageState != MessageState.STARTING) {
			return false;
		}
		RouteService.observeBridgingForRinging(getCallParam());
		messageState = MessageState.RINGING;
		return true;
	}

	@Override
	public boolean onCancel() {
		// TODO: Cancel Message
		return false;
	}

	@Override
	public boolean onACK() {
		if (messageState != MessageState.BYE) {
			return false;
		}
		onFinish();
		return false;
	}

}
