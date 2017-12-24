package sipserver.com.core.sip.handler.invite;

import java.util.Objects;

import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.sip.parameter.constant.Constant.MessageState;
import sipserver.com.core.sip.parameter.param.CallParam;
import sipserver.com.core.sip.service.RouteService;

public class InviteServerMessageHandler extends MessageHandler {

	private static Logger logger = Logger.getLogger(InviteServerMessageHandler.class);

	public InviteServerMessageHandler(Request request, Session session) {
		super(request, session);
	}

	public InviteServerMessageHandler(Request request, String remoteAddress, int remotePort) {
		super(request, remoteAddress, remotePort);
	}

	@Override
	public boolean onTrying() {
		if (!super.onTrying()) {
			logger.error("Control Your Sip Message!!");
			onFinishImmediately();
			return false;
		}
		sendResponseMessage(Response.TRYING);

		CallParam fromCallParam = new CallParam(getExtension());
		if (Objects.nonNull(getRequest().getRawContent())) {
			fromCallParam.setSdpRemoteContent(new String(getRequest().getRawContent()));
		}
		fromCallParam.setMessageHandler(this);
		setCallParam(fromCallParam);
		messageState = MessageState.TRYING;
		RouteService.route(fromCallParam);
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
		onFinishImmediately();
		return false;
	}

	@Override
	public boolean onOk() {
		if (messageState == MessageState.BYE || messageState == MessageState.CANCELING) {
			sendACK();
			onFinishImmediately();
			return true;
		}

		if (!super.onOk()) {
			messageState = MessageState.FAIL;
			return false;
		}
		messageState = MessageState.OK;
		sendResponseMessage(Response.OK, getCallParam().getSdpLocalContent());
		return true;
	}

	@Override
	public boolean onACK() {
		if (messageState == MessageState.OK) {
			messageState = MessageState.CALLING;
			return true;
		}

		if (messageState == MessageState.BUSY) {
			logger.warn("On Busy State Recieve ACK!");
			onFinishImmediately();
			return true;
		}

		if (messageState == MessageState.BYE || messageState == MessageState.CANCELING) {
			onFinishImmediately();
			return true;
		}
		return false;
	}

	@Override
	public boolean onCancel(Request cancelRequest) {
		if (!(messageState != MessageState.TRYING || messageState != MessageState.RINGING)) {
			sendResponseMessage(Response.BAD_REQUEST);
			return false;
		}
		setRequest(cancelRequest);
		messageState = MessageState.CANCELING;
		sendResponseMessage(Response.OK);
		RouteService.observeBridgingForCancel(getCallParam());
		return true;
	}

	@Override
	public boolean onFinish() {
		messageState = MessageState.FINISH;
		return false;
	}

}
