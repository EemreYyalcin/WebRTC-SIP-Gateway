package sipserver.com.core.sip.handler;

import java.util.ArrayList;
import java.util.Objects;

import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import sipserver.com.core.event.BaseEvent;
import sipserver.com.core.state.State.MessageState;
import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.parameter.constant.Constant.TransportType;
import sipserver.com.util.message.HeaderBuilder;
import sipserver.com.util.operation.MicroOperation;

public abstract class MessageHandler implements BaseEvent {

	private Request request;
	private Response response;
	private String remoteAddress;
	private int remotePort;
	private Session session;
	private Extension extension;

	public MessageState messageState = MessageState.STARTING;

	protected MessageHandler(Request request, String remoteAddress, int remotePort) {
		this.request = request;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
	}

	protected MessageHandler(Request request, Session session) {
		this.request = request;
		this.session = session;
	}

	protected void sendACK() {
		try {
			if (Objects.isNull(getResponse())) {
				throw new Exception();
			}
			FromHeader fromHeader = (FromHeader) getResponse().getHeader(FromHeader.NAME);
			ToHeader toHeader = (ToHeader) getResponse().getHeader(ToHeader.NAME);
			SipURI requestURI = HeaderBuilder.createSipUri(getExtension());
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders();
			CallIdHeader callIdHeader = (CallIdHeader) getResponse().getHeader(CallIdHeader.NAME);
			CSeqHeader responseCseq = (CSeqHeader) getResponse().getHeader(CSeqHeader.NAME);
			CSeqHeader cSeqHeader = ServerCore.getCoreElement().getHeaderFactory().createCSeqHeader(responseCseq.getSeqNumber(), Request.ACK);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70);
			Request request = ServerCore.getCoreElement().getMessageFactory().createRequest(requestURI, Request.ACK, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			ContactHeader contactHeader = HeaderBuilder.createContactHeader(getExtension());
			request.addHeader(contactHeader);
			sendMessage(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTrying() {
		try {
			if (messageState == MessageState.STARTING) {
				return false;
			}
			FromHeader fromHeader = (FromHeader) getRequest().getHeader(FromHeader.NAME);
			ToHeader toHeader = (ToHeader) getRequest().getHeader(ToHeader.NAME);
			ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
			ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);

			if (MicroOperation.isAnyNull(fromHeader, toHeader, viaHeader, contactHeader)) {
				// logger.error("Via Header is null!!");
				return false;
			}

			extension = ExtensionBuilder.getExtension(contactHeader, viaHeader);
			if (Objects.isNull(extension)) {
				sendResponseMessage(Response.FORBIDDEN);
				return false;
			}

			extension.setAddress(viaHeader.getHost());
			extension.setSession(session);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onOk(String content) {
		if (messageState == MessageState.TRYING) {
			return true;
		}
		if (messageState == MessageState.RINGING) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onBye(Request byeRequest) {
		if (messageState != MessageState.CALLING) {
			sendResponseMessage(Response.BAD_REQUEST);
			return false;
		}

		setRequest(byeRequest);
		messageState = MessageState.BYE;
		sendResponseMessage(Response.OK);
		
		// TODO: Observer Router
		return false;
	}
	
	@Override
	public boolean onBye() {
		try {
			if (messageState != MessageState.CALLING) {
				return false;
			}
			FromHeader fromHeader = null;
			ToHeader toHeader = null;
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders();
			SipURI requestURI = HeaderBuilder.createSipUri(getExtension());
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70);
			CallIdHeader callIdHeader = (CallIdHeader) getResponse().getHeader(CallIdHeader.NAME);
			CSeqHeader responseCseq = (CSeqHeader) getResponse().getHeader(CSeqHeader.NAME);
			CSeqHeader cSeqHeader = ServerCore.getCoreElement().getHeaderFactory().createCSeqHeader(responseCseq.getSeqNumber() + 1, Request.BYE);
			fromHeader = (FromHeader) getRequest().getHeader(FromHeader.NAME);
			toHeader = (ToHeader) getResponse().getHeader(ToHeader.NAME);

			Request request = ServerCore.getCoreElement().getMessageFactory().createRequest(requestURI, Request.BYE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			if (Objects.isNull(request)) {
				throw new Exception();
			}
			messageState = MessageState.BYE;
			setRequest(request);
			sendMessage(request);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	@Override
	public boolean onCancel(Request cancelRequest) {
		if (messageState != MessageState.TRYING || messageState != MessageState.RINGING) {
			sendResponseMessage(Response.BAD_REQUEST);
			return false;
		}
		setRequest(cancelRequest);
		messageState = MessageState.CANCELING;
		sendResponseMessage(Response.OK);
		return true;
	}

	protected void sendResponseMessage(int responseCode) {
		sendResponseMessage(responseCode, null);
	}

	protected void sendResponseMessage(int responseCode, String content) {
		try {
			Response response = null;
			if (Objects.nonNull(content)) {
				response = ServerCore.getCoreElement().getMessageFactory().createResponse(responseCode, getRequest(), (ContentTypeHeader) getRequest().getHeader(ContentTypeHeader.NAME), content.getBytes());
				response.setContent(content.getBytes(), ServerCore.getCoreElement().getHeaderFactory().createContentTypeHeader("application", "sdp"));
			} else {
				response = ServerCore.getCoreElement().getMessageFactory().createResponse(responseCode, getRequest());
			}
			response.addHeader(ServerCore.getCoreElement().getHeaderFactory().createAllowHeader(SipServerSharedProperties.allowHeaderValue));

			String displayName = "Anonymous";
			if (Objects.nonNull(extension)) {
				displayName = extension.getExten();
			}

			// Create the contact name address.
			SipURI contactURI = ServerCore.getCoreElement().getAddressFactory().createSipURI(displayName, ServerCore.getCoreElement().getLocalServerAddress());
			contactURI.setPort(ServerCore.getCoreElement().getLocalSipPort());

			ContactHeader contactHeader = ServerCore.getCoreElement().getHeaderFactory().createContactHeader(ServerCore.getCoreElement().getAddressFactory().createAddress(contactURI));
			response.addHeader(contactHeader);

			sendMessage(response);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void sendMessage(Message message) {
		if (Objects.nonNull(session)) {
			ServerCore.getServerCore().getTransport(TransportType.WS).sendSipMessage(message, remoteAddress, remotePort, session);
			return;
		}
		ServerCore.getServerCore().getTransport(TransportType.UDP).sendSipMessage(message, remoteAddress, remotePort, session);
	}

	protected Request getRequest() {
		return request;
	}

	protected void setRequest(Request request) {
		this.request = request;
	}

	protected Extension getExtension() {
		return extension;
	}

	protected Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

}
