package sipserver.com.core.sip.handler.options;

import javax.sip.message.Request;

import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.sip.parameter.constant.Constant.MessageState;
import sipserver.com.domain.Extension;

public class OptionsClientMessageHandler extends MessageHandler {

	public OptionsClientMessageHandler(Request request, Extension extension) {
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
		sendACK();
		messageState = MessageState.FAIL;
		onFinish();
		return false;
	}

	@Override
	public boolean onOk(String content) {
		messageState = MessageState.OK;
		getExtension().keepRegistered();
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
		return false;
	}

	@Override
	public boolean onCancel() {
		return false;
	}

	@Override
	public boolean onACK() {
		return false;
	}

}
