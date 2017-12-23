package sipserver.com.core.sip.handler.register;

import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.sip.parameter.constant.Constant.MessageState;
import sipserver.com.domain.Extension;

public class RegisterClientMessageHandler extends MessageHandler {

	public RegisterClientMessageHandler(Request request, Extension extension) {
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
		if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
			// TODO: Making unautherized
			// if (!AliasService.isHaveAuthenticateHeader(response)) {
			// return;
			// }
			// AuthenticationHelper authenticationHelper = new
			// AuthenticationHelperImpl(null, new
			// AccountManagerImpl(ServerCore.getCoreElement().getTrunkExtension(trunkExtensionLocal.getExten())),
			// getTransport().getHeaderFactory());
			// ClientTransaction clientTransaction =
			// authenticationHelper.handleChallenge(responseEvent.getResponse(),
			// responseEvent.getClientTransaction(), transport.getSipProvider(), 5, false);
			//
			// sendRequestMessage();
			return false;
		}
		if (statusCode == Response.FORBIDDEN) {
			return false;
		}
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
