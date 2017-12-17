package sipserver.com.parameter.param;

import java.util.Objects;

import javax.sip.message.Request;

import com.mgcp.transport.MgcpSession;

import sipserver.com.domain.Extension;

public class CallParam {

	private Request request;
	private Request secondrequest;
	private Extension extension;
	private String sdpLocalContent;
	private String sdpRemoteContent;

	private MgcpSession mgcpSession;
	private boolean isError = false;
	
	
	public static CallParam createCallParam(Extension extension, byte[] content) {
		CallParam callParam = new CallParam();
		callParam.setExtension(extension);
		if (Objects.nonNull(content)) {
			callParam.setSdpRemoteContent(new String(content));
		}
		return callParam;
	}
	

	public Request getRequest() {
		return request;
	}

	public CallParam setRequest(Request request) {
		this.request = request;
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

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

}
