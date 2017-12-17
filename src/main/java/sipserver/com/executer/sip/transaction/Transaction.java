package sipserver.com.executer.sip.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.constant.Constant.TransportType;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.util.message.HeaderBuilder;

public abstract class Transaction implements Base {

	private Request request;
	private Request byeRequest;
	private Request cancelRequest;
	private Response response;
	private Extension extension;
	
	private MessageHandler messageHandler;

	private int lastResponseCode = Response.GONE;
	private int currentResponseCode = Response.GONE;

	private CallParam callParam;

	private Transaction bridgeTransaction;

	public Predicate<Integer> predicateLastResponseState = t -> (t == getLastResponseCode());

	public Predicate<List<Integer>> predicateLastResponseStateList = t -> t.stream().filter(predicateLastResponseState).collect(Collectors.<Integer>toList()).size() > 0;

	public abstract void processACK();

	public Transaction(Extension extension) {
		this.extension = extension;
	}

	public void sendACK(Response response) {
		try {
			FromHeader fromHeader = (FromHeader) response.getHeader(FromHeader.NAME);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			SipURI requestURI = HeaderBuilder.createSipUri(getExtension());
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders();
			CallIdHeader callIdHeader = (CallIdHeader) response.getHeader(CallIdHeader.NAME);
			CSeqHeader responseCseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
			CSeqHeader cSeqHeader = ServerCore.getCoreElement().getHeaderFactory().createCSeqHeader(responseCseq.getSeqNumber(), Request.ACK);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70);
			Request request = ServerCore.getCoreElement().getMessageFactory().createRequest(requestURI, Request.ACK, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			ContactHeader contactHeader = HeaderBuilder.createContactHeader(getExtension());
			request.addHeader(contactHeader);
			getTransport().sendSipMessage(request, getAddress(), getPort(), getSession());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendACK() {
		sendACK(getResponse());
	}

	public void sendByeMessage() {
		try {
			FromHeader fromHeader = null;
			ToHeader toHeader = null;
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders();
			SipURI requestURI = HeaderBuilder.createSipUri(getExtension());
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70);
			CallIdHeader callIdHeader = (CallIdHeader) getResponse().getHeader(CallIdHeader.NAME);
			CSeqHeader responseCseq = (CSeqHeader) getResponse().getHeader(CSeqHeader.NAME);
			CSeqHeader cSeqHeader = ServerCore.getCoreElement().getHeaderFactory().createCSeqHeader(responseCseq.getSeqNumber() + 1, Request.BYE);

			if (this instanceof ClientTransaction) {
				fromHeader = (FromHeader) getRequest().getHeader(FromHeader.NAME);
				toHeader = (ToHeader) getResponse().getHeader(ToHeader.NAME);
			} else {
				FromHeader fromHeaderResponse = (FromHeader) getResponse().getHeader(FromHeader.NAME);
				ToHeader toHeaderResponse = (ToHeader) getResponse().getHeader(ToHeader.NAME);
				toHeader = ServerCore.getCoreElement().getHeaderFactory().createToHeader(fromHeaderResponse.getAddress(), fromHeaderResponse.getTag());
				fromHeader = ServerCore.getCoreElement().getHeaderFactory().createFromHeader(toHeaderResponse.getAddress(), toHeaderResponse.getTag());

			}

			Request request = ServerCore.getCoreElement().getMessageFactory().createRequest(requestURI, Request.BYE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

			if (Objects.nonNull(getTransport())) {
				getTransport().sendSipMessage(request, getAddress(), getPort(), getSession());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public int getPort() {
		return getExtension().getPort();
	}

	public String getCallId() {
		return ((CallIdHeader) getRequest().getHeader(CallIdHeader.NAME)).getCallId();
	}

	public Extension getExtension() {
		return extension;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public Session getSession() {
		return getExtension().getSession();
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

	public TransportType getTransportType() {
		return getExtension().getTransportType();
	}

	public String getAddress() {
		return getExtension().getAddress();
	}

	public SipServerTransport getTransport() {
		return ServerCore.getServerCore().getTransport(getTransportType());
	}

	public int getLastResponseCode() {
		return lastResponseCode;
	}

	public int getCurrentResponseCode() {
		return currentResponseCode;
	}

	public void setCurrentResponseCode(int currentResponseCode) {
		this.lastResponseCode = this.currentResponseCode;
		this.currentResponseCode = currentResponseCode;
	}

	public Request getByeRequest() {
		return byeRequest;
	}

	public void setByeRequest(Request byeRequest) {
		this.byeRequest = byeRequest;
	}

	public Request getCancelRequest() {
		return cancelRequest;
	}

	public void setCancelRequest(Request cancelRequest) {
		this.cancelRequest = cancelRequest;
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
}
