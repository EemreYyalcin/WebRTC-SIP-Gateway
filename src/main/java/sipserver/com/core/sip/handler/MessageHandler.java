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

import org.apache.log4j.Logger;

import sipserver.com.core.event.BaseEvent;
import sipserver.com.core.sip.builder.HeaderBuilder;
import sipserver.com.core.sip.parameter.constant.Constant.MessageState;
import sipserver.com.core.sip.parameter.param.CallParam;
import sipserver.com.core.sip.service.RouteService;
import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.starter.ServerCore;
import sipserver.com.executer.starter.SipServerSharedProperties;
import sipserver.com.util.operation.MicroOperation;

public abstract class MessageHandler implements BaseEvent {

	private static Logger logger = Logger.getLogger(MessageHandler.class);

	private Request request;
	private Response response;
	private String remoteAddress;
	private int remotePort;
	private Session session;
	private Extension extension;

	private CallParam callParam;

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

	protected MessageHandler(Request request, Extension extension) {
		this.request = request;
		this.extension = extension;
		this.remoteAddress = extension.getAddress();
		this.remotePort = extension.getPort();
		this.session = extension.getSession();
	}

	protected void sendACK() {
		try {
			if (Objects.isNull(getResponse())) {
				throw new Exception();
			}
			FromHeader fromHeader = (FromHeader) getResponse().getHeader(FromHeader.NAME);
			ToHeader toHeader = (ToHeader) getResponse().getHeader(ToHeader.NAME);
			SipURI requestURI = HeaderBuilder.createSipUri(getExtension());
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders(Objects.nonNull(getExtension().getSession()));
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
			if (messageState != MessageState.STARTING) {
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
	public boolean onOk() {
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
		RouteService.observeBridgingForBye(getCallParam());
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
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders(Objects.nonNull(getExtension().getSession()));
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
		return true;
	}

	@Override
	public boolean onFinishImmediately() {
		ServerCore.getCoreElement().removeHandler(((CallIdHeader) getRequest().getHeader(CallIdHeader.NAME)).getCallId());
		onFinish();
		return true;
	}

	@Override
	public boolean onCancel() {
		logger.error("onCancel Request Error!!");
		return false;
	}

	@Override
	public boolean onCancel(Request cancelRequest) {
		logger.error("onCancel Request Error with Parameter request!!");
		return false;
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
		if (message instanceof Response) {
			setResponse((Response)message);
		}
		ServerCore.getServerCore().getTransport(Objects.nonNull(session)).sendSipMessage(message, remoteAddress, remotePort, session);
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

	public CallParam getToCallParam() {
		Extension toExtenFromHeaderExtension = ExtensionBuilder.getExtension((ToHeader) getRequest().getHeader(ToHeader.NAME));
		if (Objects.isNull(toExtenFromHeaderExtension)) {
			return null;
		}
		return new CallParam(toExtenFromHeaderExtension);
	}

	public CallParam getCallParam() {
		return callParam;
	}

	public void setCallParam(CallParam callParam) {
		this.callParam = callParam;
	}

}
