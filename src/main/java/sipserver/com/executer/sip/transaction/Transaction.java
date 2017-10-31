package sipserver.com.executer.sip.transaction;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;

import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.noyan.Base;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.control.ChannelControlService;
import sipserver.com.service.operational.BridgeService;
import sipserver.com.service.util.message.HeaderBuilder;

public class Transaction implements Base {

	private Request request;
	private Response response;
	private InetAddress address;
	private int port = SipServerSharedProperties.blankCode;
	private String callId;

	private Extension extension;

	private SipServerTransport transport;

	public void processByeOrCancelRequest(Request request) {
		try {
			CallParam fromCallParam = ChannelControlService.getChannel(getCallId());
			if (Objects.isNull(fromCallParam)) {
				error("Channel Not Found " + request.getHeader(FromHeader.NAME).toString());
				return;
			}
			Response response = getTransport().getMessageFactory().createResponse(Response.OK, request);
			if (Objects.nonNull(getTransport())) {
				getTransport().sendData(response.toString(), getAddress(), getPort());
			}
			if (request.getMethod().equals(Request.BYE)) {
				BridgeService.bye(fromCallParam);
				return;
			}
			//For Cancel
			Response responseRequestTerminated = getTransport().getMessageFactory().createResponse(Response.REQUEST_TERMINATED, getRequest());
			if (Objects.nonNull(getTransport())) {
				getTransport().sendData(responseRequestTerminated.toString(), getAddress(), getPort());
			}
			
			BridgeService.cancel(fromCallParam);

		} catch (Exception e) {
			error(e);
		}
	}

	public void sendByeMessage() {
		try {
			FromHeader fromHeader = null;
			ToHeader toHeader = null;
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders(getExtension().getTransport());
			SipURI requestURI = HeaderBuilder.createSipUri(getExtension());
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70, getExtension().getTransport());
			CallIdHeader callIdHeader = (CallIdHeader) getResponse().getHeader(CallIdHeader.NAME);
			CSeqHeader responseCseq = (CSeqHeader) getResponse().getHeader(CSeqHeader.NAME);
			CSeqHeader cSeqHeader = getExtension().getTransport().getHeaderFactory().createCSeqHeader(responseCseq.getSeqNumber() + 1, Request.BYE);

			if (this instanceof ClientTransaction) {
				fromHeader = (FromHeader) getRequest().getHeader(FromHeader.NAME);
				toHeader = (ToHeader) getResponse().getHeader(ToHeader.NAME);
			} else {
				FromHeader fromHeaderResponse = (FromHeader) getResponse().getHeader(FromHeader.NAME);
				ToHeader toHeaderResponse = (ToHeader) getResponse().getHeader(ToHeader.NAME);
				toHeader = getTransport().getHeaderFactory().createToHeader(fromHeaderResponse.getAddress(), fromHeaderResponse.getTag());
				fromHeader = getTransport().getHeaderFactory().createFromHeader(toHeaderResponse.getAddress(), toHeaderResponse.getTag());

			}

			Request request = getExtension().getTransport().getMessageFactory().createRequest(requestURI, Request.BYE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

			if (Objects.nonNull(getTransport())) {
				getTransport().sendData(request.toString(), getAddress(), getPort());
			}
		} catch (Exception e) {
			error(e);
		}
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public SipServerTransport getTransport() {
		return transport;
	}

	public void setTransport(SipServerTransport transport) {
		this.transport = transport;
	}

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

}
