package sipserver.com.executer.sip.transaction;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;

import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import com.noyan.Base;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
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
	private Session session;

	private CallParam callParam;

	private Transaction bridgeTransaction;

	public void processByeOrCancelRequest(Request request) {
		try {
			if (Objects.isNull(getCallParam())) {
				error("Channel Not Found " + request.getHeader(FromHeader.NAME).toString());
				return;
			}
			Response response = getTransport().getMessageFactory().createResponse(Response.OK, request);
			getTransport().sendSipMessage(response, getAddress(), getPort(), getSession());
			if (request.getMethod().equals(Request.BYE)) {
				BridgeService.bye(this);
				return;
			}
			// For Cancel
			Response responseRequestTerminated = getTransport().getMessageFactory().createResponse(Response.REQUEST_TERMINATED, getRequest());
			if (Objects.nonNull(getTransport())) {
				getTransport().sendSipMessage(responseRequestTerminated, getAddress(), getPort(), getSession());
			}
			if (this instanceof ClientTransaction) {
				BridgeService.cancel((ClientTransaction) this);
			}

		} catch (Exception e) {
			error(e);
		}
	}

	public void sendACK() {
		try {
			if (Objects.isNull(getResponse())) {
				return;
			}
			FromHeader fromHeader = (FromHeader) getResponse().getHeader(FromHeader.NAME);
			ToHeader toHeader = (ToHeader) getResponse().getHeader(ToHeader.NAME);
			SipURI requestURI = HeaderBuilder.createSipUri(getExtension());
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders(getExtension().getTransport());
			CallIdHeader callIdHeader = (CallIdHeader) getResponse().getHeader(CallIdHeader.NAME);
			CSeqHeader responseCseq = (CSeqHeader) getResponse().getHeader(CSeqHeader.NAME);
			CSeqHeader cSeqHeader = getExtension().getTransport().getHeaderFactory().createCSeqHeader(responseCseq.getSeqNumber(), Request.ACK);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70, getExtension().getTransport());
			Request request = getExtension().getTransport().getMessageFactory().createRequest(requestURI, Request.ACK, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			ContactHeader contactHeader = HeaderBuilder.createContactHeader(getExtension());
			request.addHeader(contactHeader);
			getTransport().sendSipMessage(request, getAddress(), getPort(), getSession());
			if (getRequest().getMethod().equals(Request.INVITE)) {
				return;
			}
			// ServerCore.getCoreElement().removeTransaction(getCallId());
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
				getTransport().sendSipMessage(request, getAddress(), getPort(), getSession());
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

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public CallParam getCallParam() {
		return callParam;
	}

	public void setCallParam(CallParam callParam) {
		this.callParam = callParam;
	}

	public Transaction getBridgeTransaction() {
		return bridgeTransaction;
	}

	public void setBridgeTransaction(Transaction bridgeTransaction) {
		this.bridgeTransaction = bridgeTransaction;
	}

}
