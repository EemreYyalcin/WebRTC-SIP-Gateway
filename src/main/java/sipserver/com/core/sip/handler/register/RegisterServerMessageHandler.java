package sipserver.com.core.sip.handler.register;

import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import sipserver.com.core.sip.builder.HeaderBuilder;
import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.sip.parameter.constant.Constant.MessageState;
import sipserver.com.executer.starter.ServerCore;

public class RegisterServerMessageHandler extends MessageHandler {

	private static Logger logger = Logger.getLogger(RegisterServerMessageHandler.class);

	public RegisterServerMessageHandler(Request request, Session session) {
		super(request, session);
	}

	public RegisterServerMessageHandler(Request request, String remoteAddress, int remotePort) {
		super(request, remoteAddress, remotePort);
	}

	@Override
	public boolean onTrying() {
		try {
			if (!super.onTrying()) {
				return false;
			}
			sendResponseMessage(Response.TRYING);

			if (getExtension().isRegister()) {
				getExtension().keepRegistered();
				logger.info("Exten:" + getExtension().getExten() + " Keep Register.");
				sendResponseMessage(Response.OK);
				messageState = MessageState.OK;
				onFinish();
				return true;
			}

			if (Objects.isNull(getExtension().getPass())) {
				sendResponseMessage(Response.FORBIDDEN);
				logger.info("Forbidden Peer Password has not setted Exten:" + getExtension().getExten());
				onFinish();
				return true;
			}

			if (!HeaderBuilder.isHaveAuthenticateHeader(getRequest())) {
				Response challengeResponse = ServerCore.getCoreElement().getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, getRequest());
				ServerCore.getCoreElement().getDigestServerAuthentication().generateChallenge(ServerCore.getCoreElement().getHeaderFactory(), challengeResponse, "nist.gov");
				ProxyAuthenticateHeader proxyAuthenticateHeader = (ProxyAuthenticateHeader) challengeResponse.getHeader(ProxyAuthenticateHeader.NAME);
				Objects.requireNonNull(proxyAuthenticateHeader);
				proxyAuthenticateHeader.setParameter("username", getExtension().getExten());
				sendMessage(challengeResponse);
				onFinish();
				return true;
			}

			if (!ServerCore.getCoreElement().getDigestServerAuthentication().doAuthenticatePlainTextPassword(getRequest(), getExtension().getPass())) {
				sendResponseMessage(Response.FORBIDDEN);
				logger.info("Forbidden Peer Wrong Password Exten:" + getExtension().getExten());
				onFinish();
				return true;
			}
			getExtension().keepRegistered();
			sendResponseMessage(Response.OK);
			logger.info("Peer Registerd Exten:" + getExtension().getExten());
		} catch (Exception e) {
			e.printStackTrace();
			sendResponseMessage(Response.BAD_EVENT);
			onFinish();
		}

		return true;
	}

	@Override
	public boolean onReject(int statusCode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFinish() {
		messageState = MessageState.FINISH;
		return false;
	}

	@Override
	public boolean onRinging() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onCancel() {
		// TODO Auto-generated method stub
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
