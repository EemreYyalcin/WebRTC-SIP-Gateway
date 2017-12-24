package sipserver.com.core.sip.handler.invite;

import java.util.Objects;

import javax.sip.message.Request;

import org.apache.log4j.Logger;

import sipserver.com.core.sip.builder.MessageBuilder;
import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.sip.parameter.constant.Constant.MessageState;
import sipserver.com.core.sip.service.RouteService;
import sipserver.com.domain.Extension;

public class InviteClientMessageHandler extends MessageHandler {

	private static Logger logger = Logger.getLogger(InviteClientMessageHandler.class);
	
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
		messageState = MessageState.BUSY;
		RouteService.observeBridgingForReject(getCallParam(), statusCode);
		onFinishImmediately();
		return false;
	}

	@Override
	public boolean onOk() {
		if (messageState == MessageState.BYE) {
			RouteService.observeBridgingForBye(getCallParam());
			sendACK();
			onFinishImmediately();
			return true;
		}
		if (messageState == MessageState.CANCELING) {
			sendACK();
			onFinishImmediately();
			return true;
		}
		if (!super.onOk()) {
			messageState = MessageState.FAIL;
			onFinishImmediately();
			//TODO: Line is closed immediately
			return false;
		}
		RouteService.observeBridgingForAcceptCall(getCallParam());
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
		messageState = MessageState.CANCELING;
		Request cancelRequest = MessageBuilder.createCancelMessage(getRequest(), getExtension());
		if (Objects.isNull(cancelRequest)) {
			onFinishImmediately();
			logger.error("Error Cancel Message Create");
			return false;
		}
		sendMessage(cancelRequest);
		return true;
	}

	@Override
	public boolean onACK() {
		if (messageState != MessageState.BYE) {
			return false;
		}
		onFinishImmediately();
		return false;
	}

}
