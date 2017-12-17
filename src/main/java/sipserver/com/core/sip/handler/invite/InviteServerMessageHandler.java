package sipserver.com.core.sip.handler.invite;

import java.util.Objects;

import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.state.State.MessageState;
import sipserver.com.parameter.param.CallParam;

public class InviteServerMessageHandler extends MessageHandler {

	public InviteServerMessageHandler(Request request, Session session) {
		super(request, session);
	}

	public InviteServerMessageHandler(Request request, String remoteAddress, int remotePort) {
		super(request, remoteAddress, remotePort);
	}

	@Override
	public boolean onTrying() {
		if (!super.onTrying()) {
			return false;
		}
		sendResponseMessage(Response.TRYING);

		CallParam fromCallParam = CallParam.createCallParam(getExtension(), getRequest().getRawContent());
		if (Objects.isNull(fromCallParam)) {
			return onReject(Response.BUSY_HERE);
		}

		messageState = MessageState.TRYING;
		// RouteService.route(this, toHeader);
		return true;
	}

	@Override
	public boolean onRinging() {
		if (messageState != MessageState.TRYING) {
			return false;
		}
		messageState = MessageState.RINGING;
		sendResponseMessage(Response.RINGING);
		return true;
	}

	@Override
	public boolean onReject(int statusCode) {
		messageState = MessageState.BUSY;
		sendResponseMessage(Response.BUSY_HERE);
		return false;
	}

	@Override
	public boolean onOk(String content) {
		if (messageState == MessageState.BYE || messageState == MessageState.CANCELING) {
			onFinish();
			return true;
		}

		if (!super.onOk(content)) {
			return false;
		}
		messageState = MessageState.OK;
		sendResponseMessage(Response.OK, content);
		return true;
	}


	@Override
	public boolean onACK() {
		if (messageState == MessageState.OK) {
			messageState = MessageState.CALLING;
			return true;
		}

		if (messageState == MessageState.BUSY) {
			onFinish();
			return true;
		}

		if (messageState == MessageState.BYE) {
			onFinish();
			return true;
		}
		return false;
	}

	@Override
	public boolean onFinish() {
		messageState = MessageState.FINISH;
		return false;
	}


	@Override
	public boolean onCancel() {
		// TODO Auto-generated method stub
		return false;
	}

}
