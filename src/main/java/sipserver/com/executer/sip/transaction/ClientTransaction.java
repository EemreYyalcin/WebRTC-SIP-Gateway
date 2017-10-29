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
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.util.message.HeaderBuilder;

public abstract class ClientTransaction extends Transaction {

	public void sendRequestMessage() {
		getTransport().sendData(getRequest().toString(), getAddress(), getPort());
	}

	public void sendRequestMessage(Request request) {
		getTransport().sendData(request.toString(), getAddress(), getPort());
	}

	public abstract void processResponse(Response response);

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
			sendRequestMessage(request);
		} catch (Exception e) {
			error(e);
		}

	}

	public static Request createRequestMessage(String method, Extension extension) {
		try {
			FromHeader fromHeader = null;
			if (method.equals(Request.OPTIONS)) {
				Extension serverExtension = ExtensionBuilder.createExtension("Unknown", "Unknown", ServerCore.getCoreElement().getLocalServerAddress(), ServerCore.getCoreElement().getLocalSipPort());
				Objects.requireNonNull(serverExtension);
				serverExtension.setTransport(extension.getTransport());
				fromHeader = HeaderBuilder.createFromHeader(serverExtension);
			}
			if (Objects.isNull(fromHeader)) {
				fromHeader = HeaderBuilder.createFromHeader(extension);
			}
			Objects.requireNonNull(fromHeader);
			ToHeader toHeader = HeaderBuilder.createToHeader(extension);
			Objects.requireNonNull(toHeader);
			RouteHeader routeHeader = HeaderBuilder.createRouteHeader(extension);
			Objects.requireNonNull(routeHeader);
			CallIdHeader callIdHeader = HeaderBuilder.createCallIdHeader();
			Objects.requireNonNull(callIdHeader);
			CSeqHeader cSeqHeader = HeaderBuilder.createCseqHeader(extension.getTransport(), method);
			Objects.requireNonNull(cSeqHeader);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70, extension.getTransport());
			Objects.requireNonNull(maxForwards);
			Request request = extension.getTransport().getMessageFactory().createRequest(HeaderBuilder.createSipUri(extension), method, callIdHeader, cSeqHeader, fromHeader, toHeader, HeaderBuilder.createViaHeaders(extension.getTransport()), maxForwards);
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

	public static Request createInviteMessage(CallParam toCallParam) {
		try {
			FromHeader fromHeader = HeaderBuilder.createFromHeader(toCallParam.getBridgeCallParam().getExtension());
			Objects.requireNonNull(fromHeader);
			ToHeader toHeader = HeaderBuilder.createToHeader(toCallParam.getExtension());
			Objects.requireNonNull(toHeader);
			SipURI requestURI = HeaderBuilder.createSipUri(toCallParam.getExtension());
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders(toCallParam.getExtension().getTransport());
			CallIdHeader callIdHeader = HeaderBuilder.createCallIdHeader();
			Objects.requireNonNull(callIdHeader);
			CSeqHeader cSeqHeader = HeaderBuilder.createCseqHeader(toCallParam.getExtension().getTransport(), Request.INVITE);
			Objects.requireNonNull(cSeqHeader);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70, toCallParam.getExtension().getTransport());
			Objects.requireNonNull(maxForwards);
			Request request = toCallParam.getExtension().getTransport().getMessageFactory().createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
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
