package sipserver.com.executer.sip.transaction;

import java.util.ArrayList;
import java.util.Objects;

import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.util.message.HeaderBuilder;

public abstract class ClientTransaction extends Transaction {

	public abstract void processResponse(Response response);

	public void sendCancelMessage() {
		try {
			SipServerTransport transport = ServerCore.getServerCore().getTransport(getExtension().getTransportType());
			FromHeader fromHeader = (FromHeader) getRequest().getHeader(FromHeader.NAME);
			Objects.requireNonNull(fromHeader);
			ToHeader toHeader = (ToHeader) getRequest().getHeader(ToHeader.NAME);
			Objects.requireNonNull(toHeader);
			SipURI requestURI = HeaderBuilder.createSipUri(getExtension());
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			viaHeaders.add((ViaHeader) getRequest().getHeader(ViaHeader.NAME));
			CallIdHeader callIdHeader = (CallIdHeader) getRequest().getHeader(CallIdHeader.NAME);
			Objects.requireNonNull(callIdHeader);
			CSeqHeader cSeqHeaderRequest = (CSeqHeader) getRequest().getHeader(CSeqHeader.NAME);
			Objects.requireNonNull(cSeqHeaderRequest);
			CSeqHeader cseqHeader = getTransport().getHeaderFactory().createCSeqHeader(cSeqHeaderRequest.getSeqNumber(), Request.CANCEL);
			Objects.requireNonNull(cseqHeader);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70, transport);
			Objects.requireNonNull(maxForwards);
			Request request = transport.getMessageFactory().createRequest(requestURI, Request.CANCEL, callIdHeader, cseqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			Objects.requireNonNull(request);
			getTransport().sendSipMessage(request, getAddress(), getPort(), getSession());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Request createRequestMessage(String method, Extension extension) {
		try {
			FromHeader fromHeader = null;
			if (method.equals(Request.OPTIONS)) {
				Extension serverExtension = ExtensionBuilder.createExtension("Unknown", "Unknown", ServerCore.getCoreElement().getLocalServerAddress(), ServerCore.getCoreElement().getLocalSipPort());
				Objects.requireNonNull(serverExtension);
				serverExtension.setTransportType(extension.getTransportType());
				fromHeader = HeaderBuilder.createFromHeader(serverExtension);
			}
			if (Objects.isNull(fromHeader)) {
				fromHeader = HeaderBuilder.createFromHeader(extension);
			}
			SipServerTransport transport = ServerCore.getServerCore().getTransport(extension.getTransportType());
			Objects.requireNonNull(fromHeader);
			ToHeader toHeader = HeaderBuilder.createToHeader(extension);
			Objects.requireNonNull(toHeader);
			RouteHeader routeHeader = HeaderBuilder.createRouteHeader(extension);
			Objects.requireNonNull(routeHeader);
			CallIdHeader callIdHeader = HeaderBuilder.createCallIdHeader();
			Objects.requireNonNull(callIdHeader);
			CSeqHeader cSeqHeader = transport.getHeaderFactory().createCSeqHeader(SipServerSharedProperties.cseqSequence++, method);
			Objects.requireNonNull(cSeqHeader);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70, transport);
			Objects.requireNonNull(maxForwards);
			Request request = transport.getMessageFactory().createRequest(HeaderBuilder.createSipUri(extension), method, callIdHeader, cSeqHeader, fromHeader, toHeader, HeaderBuilder.createViaHeaders(transport), maxForwards);
			Objects.requireNonNull(request);
			ContactHeader contactHeader = HeaderBuilder.createContactHeader(extension);
			Objects.requireNonNull(contactHeader);
			request.addHeader(contactHeader);
			request.setHeader(routeHeader);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Request createInviteMessage(CallParam toCallParam, CallParam fromCallParam) {
		try {
			SipServerTransport transport = ServerCore.getServerCore().getTransport(toCallParam.getExtension().getTransportType());
			FromHeader fromHeader = HeaderBuilder.createFromHeader(fromCallParam.getExtension());
			Objects.requireNonNull(fromHeader);
			ToHeader toHeader = HeaderBuilder.createToHeader(toCallParam.getExtension());
			Objects.requireNonNull(toHeader);
			SipURI requestURI = HeaderBuilder.createSipUri(toCallParam.getExtension());
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders(transport);
			CallIdHeader callIdHeader = HeaderBuilder.createCallIdHeader();
			Objects.requireNonNull(callIdHeader);
			CSeqHeader cSeqHeader = HeaderBuilder.createCseqHeader(transport, Request.INVITE);
			Objects.requireNonNull(cSeqHeader);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70, transport);
			Objects.requireNonNull(maxForwards);
			Request request = transport.getMessageFactory().createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			Objects.requireNonNull(request);
			ContactHeader contactHeader = HeaderBuilder.createContactHeader(toCallParam.getExtension());
			request.addHeader(contactHeader);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
