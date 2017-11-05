package sipserver.com.parameter.param;

import javax.sip.message.Request;

import com.mgcp.transport.MgcpSession;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.Transaction;

public class CallParam {

	private Request request;
	private Request secondrequest;
	private Transaction transaction;
	private Extension extension;
	private String sdpLocalContent;
	private String sdpRemoteContent;
	
	private MgcpSession mgcpSession;
	

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

	public Request getSecondrequest() {
		return secondrequest;
	}

	public void setSecondrequest(Request secondrequest) {
		this.secondrequest = secondrequest;
	}

	public MgcpSession getMgcpSession() {
		return mgcpSession;
	}

	public void setMgcpSession(MgcpSession mgcpSession) {
		this.mgcpSession = mgcpSession;
	}

}
