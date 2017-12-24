package sipserver.com.core.sip.builder;

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

import sipserver.com.core.sip.parameter.param.CallParam;
import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.starter.ServerCore;
import sipserver.com.executer.starter.SipServerSharedProperties;

public class MessageBuilder {

	public static Request createInviteMessage(CallParam toCallParam, CallParam fromCallParam) {
		try {
			FromHeader fromHeader = HeaderBuilder.createFromHeader(fromCallParam.getExtension());
			Objects.requireNonNull(fromHeader);
			ToHeader toHeader = HeaderBuilder.createToHeader(toCallParam.getExtension());
			Objects.requireNonNull(toHeader);
			SipURI requestURI = HeaderBuilder.createSipUri(toCallParam.getExtension());
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders(Objects.nonNull(toCallParam.getExtension().getSession()));
			CallIdHeader callIdHeader = HeaderBuilder.createCallIdHeader();
			Objects.requireNonNull(callIdHeader);
			CSeqHeader cSeqHeader = HeaderBuilder.createCseqHeader(Request.INVITE);
			Objects.requireNonNull(cSeqHeader);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70);
			Objects.requireNonNull(maxForwards);
			Request request = ServerCore.getCoreElement().getMessageFactory().createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			Objects.requireNonNull(request);
			ContactHeader contactHeader = HeaderBuilder.createContactHeader(toCallParam.getExtension());
			request.addHeader(contactHeader);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Request createOptionsMessage(Extension extension) {
		return createRequestMessage(Request.OPTIONS, extension);
	}

	public static Request createRegisterMessage(Extension extension) {
		return createRequestMessage(Request.REGISTER, extension);
	}

	private static Request createRequestMessage(String method, Extension extension) {
		try {
			FromHeader fromHeader = null;
			if (method.equals(Request.OPTIONS)) {
				Extension serverExtension = ExtensionBuilder.createExtension("Unknown", "Unknown", ServerCore.getCoreElement().getLocalServerAddress(), ServerCore.getCoreElement().getLocalSipPort());
				Objects.requireNonNull(serverExtension);
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
			CSeqHeader cSeqHeader = ServerCore.getCoreElement().getHeaderFactory().createCSeqHeader(SipServerSharedProperties.cseqSequence++, method);
			Objects.requireNonNull(cSeqHeader);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70);
			Objects.requireNonNull(maxForwards);
			Request request = ServerCore.getCoreElement().getMessageFactory().createRequest(HeaderBuilder.createSipUri(extension), method, callIdHeader, cSeqHeader, fromHeader, toHeader, HeaderBuilder.createViaHeaders(Objects.nonNull(extension.getSession())), maxForwards);
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

	public static Request createCancelMessage(Request baseRequest, Extension toExtension) {
		try {
			FromHeader fromHeader = (FromHeader) baseRequest.getHeader(FromHeader.NAME);
			Objects.requireNonNull(fromHeader);
			ToHeader toHeader = (ToHeader) baseRequest.getHeader(ToHeader.NAME);
			Objects.requireNonNull(toHeader);
			SipURI requestURI = HeaderBuilder.createSipUri(toExtension);
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			viaHeaders.add((ViaHeader) baseRequest.getHeader(ViaHeader.NAME));
			CallIdHeader callIdHeader = (CallIdHeader) baseRequest.getHeader(CallIdHeader.NAME);
			Objects.requireNonNull(callIdHeader);
			CSeqHeader cSeqHeaderRequest = (CSeqHeader) baseRequest.getHeader(CSeqHeader.NAME);
			Objects.requireNonNull(cSeqHeaderRequest);
			CSeqHeader cseqHeader = ServerCore.getCoreElement().getHeaderFactory().createCSeqHeader(cSeqHeaderRequest.getSeqNumber(), Request.CANCEL);
			Objects.requireNonNull(cseqHeader);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70);
			Objects.requireNonNull(maxForwards);
			Request cancelRequest = ServerCore.getCoreElement().getMessageFactory().createRequest(requestURI, Request.CANCEL, callIdHeader, cseqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			Objects.requireNonNull(cancelRequest);
			return cancelRequest;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
