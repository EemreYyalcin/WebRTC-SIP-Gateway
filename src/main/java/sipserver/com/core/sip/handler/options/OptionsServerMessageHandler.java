package sipserver.com.core.sip.handler.options;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.sip.parameter.constant.Constant.MessageState;
import sipserver.com.executer.starter.ServerCore;

public class OptionsServerMessageHandler extends MessageHandler {

	private static Logger logger = Logger.getLogger(OptionsServerMessageHandler.class);

	public OptionsServerMessageHandler(Request request, Session session) {
		super(request, session);
	}

	public OptionsServerMessageHandler(Request request, String remoteAddress, int remotePort) {
		super(request, remoteAddress, remotePort);
	}

	@Override
	public boolean onTrying() {
		try {
			if (!super.onTrying()) {
				onFinish();
				return false;
			}
			sendResponseMessage(Response.TRYING);
			logger.debug("Keep Alive Exten:" + getExtension().getExten());
			sendResponseMessage(Response.OK);
			getExtension().keepRegistered();
		} catch (Exception e) {
			e.printStackTrace();
			sendResponseMessage(Response.BAD_EVENT);
			onFinish();
		}
		return true;
	}

	@Override
	public boolean onReject(int statusCode) {
		return false;
	}

	@Override
	public boolean onFinish() {
		messageState = MessageState.FINISH;
		ServerCore.getCoreElement().removeHandler(((CallIdHeader) getRequest().getHeader(CallIdHeader.NAME)).getCallId());
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
		logger.trace("OnACK Recieved ");
		ServerCore.getCoreElement().removeHandler(((CallIdHeader) getRequest().getHeader(CallIdHeader.NAME)).getCallId());
		onFinish();
		return false;
	}

}
