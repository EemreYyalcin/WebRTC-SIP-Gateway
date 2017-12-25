package sipserver.com.core.sip.parameter.param;

import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.domain.Extension;

public class CallParam {

	private String sdpLocalContent;
	private String sdpRemoteContent;
	private MessageHandler messageHandler;
	private Extension extension;

	private boolean isError = false;

	public CallParam(Extension extension) {
		this.extension = extension;
	}

	public String getSdpLocalContent() {
		return sdpLocalContent;
	}

	public void setSdpLocalContent(String sdpLocalContent) {
		this.sdpLocalContent = sdpLocalContent;
	}

	public String getSdpRemoteContent() {
		return sdpRemoteContent;
	}

	public void setSdpRemoteContent(String sdpRemoteContent) {
		this.sdpRemoteContent = sdpRemoteContent;
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public Extension getExtension() {
		return extension;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

}
