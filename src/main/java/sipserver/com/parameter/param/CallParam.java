package sipserver.com.parameter.param;

import javax.sip.Transaction;
import javax.sip.message.Request;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.SipServerSharedProperties;

public class CallParam {

	private Request request;
	private Transaction transaction;
	private Extension extension;
	private int responseCode = SipServerSharedProperties.errorResponseCode;
	private boolean isRecievedResponse = false;
	private String sdpLocalContent;
	private String sdpRemoteContent;
	
	private CallParam bridgeCallParam;
	
	
	public Request getRequest() {
		return request;
	}

	public CallParam setRequest(Request request) {
		this.request = request;
		return this;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public CallParam setTransaction(Transaction transaction) {
		this.transaction = transaction;
		return this;
	}

	public Extension getExtension() {
		return extension;
	}

	public CallParam setExtension(Extension extension) {
		this.extension = extension;
		return this;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public CallParam setResponseCode(int responseCode) {
		this.responseCode = responseCode;
		return this;
	}

	public boolean isRecievedResponse() {
		return isRecievedResponse;
	}

	public CallParam setRecievedResponse(boolean isRecievedResponse) {
		this.isRecievedResponse = isRecievedResponse;
		return this;
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

	public CallParam getBridgeCallParam() {
		return bridgeCallParam;
	}

	public void setBridgeCallParam(CallParam bridgeCallParam) {
		this.bridgeCallParam = bridgeCallParam;
	}

}
