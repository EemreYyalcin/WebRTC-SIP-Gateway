package sipserver.com.core.sip.handler.invite;

import javax.sip.message.Request;
import javax.websocket.Session;

import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.state.State.MessageState;

public class InviteClientMessageHandler extends MessageHandler {

	public InviteClientMessageHandler(Request request, String remoteAddress, int remotePort) {
		super(request, remoteAddress, remotePort);
		sendMessage(request);
	}

	public InviteClientMessageHandler(Request request, Session session) {
		super(request, session);
		sendMessage(request);
	}

	@Override
	public boolean onTrying() {
		messageState = MessageState.TRYING;
		return true;
	}

	@Override
	public boolean onReject(int statusCode) {
		// TODO: Observer Router
		// ?? SendACK ??
		messageState = MessageState.BUSY;
		onFinish();
		return false;
	}

	@Override
	public boolean onOk(String content) {
		if (messageState == MessageState.BYE) {
			sendACK();
			onFinish();
			return true;
		}
		if (!super.onOk(content)) {
			return false;
		}
		messageState = MessageState.CALLING;
		// TODO: Observer Router
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
		// TODO: Observer Router
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
