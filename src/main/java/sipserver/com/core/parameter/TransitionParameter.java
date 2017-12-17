package sipserver.com.core.parameter;

import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.SipServerSharedProperties;

public class TransitionParameter {

	private Request request;
	private Response response;
	private int responseCode = SipServerSharedProperties.errorResponseCode;
	private String content;
	private Extension extension;
	
	public Response getResponse() {
		return response;
	}

	public TransitionParameter setResponse(Response response) {
		this.response = response;
		return this;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public TransitionParameter setResponseCode(int responseCode) {
		this.responseCode = responseCode;
		return this;
	}

	public String getContent() {
		return content;
	}

	public TransitionParameter setContent(String content) {
		this.content = content;
		return this;
	}

	public Extension getExtension() {
		return extension;
	}

	public TransitionParameter setExtension(Extension extension) {
		this.extension = extension;
		return this;
	}

	public Request getRequest() {
		return request;
	}

	public TransitionParameter setRequest(Request request) {
		this.request = request;
		return this;
	}
	
	
}
