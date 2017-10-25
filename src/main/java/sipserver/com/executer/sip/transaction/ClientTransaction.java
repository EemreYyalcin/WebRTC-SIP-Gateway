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
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.util.message.HeaderBuilder;

public abstract class ClientTransaction extends Transaction {

	public ClientTransaction(Request request, InetAddress address, int port, SipServerTransport transport) {
		super(request, address, port, transport);
	}

	public void sendRequestMessage() {
		getTransport().sendData(getRequest().toString(), getAddress(), getPort());
	}

	public abstract void processResponse(Response response);

	public static Request createRequestMessage(String method, Extension extension) {
		try {
			FromHeader fromHeader = null;
			if (method.equals(Request.OPTIONS)) {
				fromHeader = HeaderBuilder.createFromHeader(new Extension("Unknown", "Unknown", ServerCore.getCoreElement().getLocalServerAddress(), ServerCore.getCoreElement().getLocalSipPort()));
			}
			if (Objects.isNull(fromHeader)) {
				fromHeader = HeaderBuilder.createFromHeader(extension);
			}
			ToHeader toHeader = HeaderBuilder.createToHeader(extension);
			RouteHeader routeHeader = HeaderBuilder.createRouteHeader(extension);
			CallIdHeader callIdHeader = HeaderBuilder.createCallIdHeader();
			CSeqHeader cSeqHeader = HeaderBuilder.createCseqHeader(extension.getTransport(), method);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70, extension.getTransport());
			Request request = extension.getTransport().getMessageFactory().createRequest(HeaderBuilder.createSipUri(extension), method, callIdHeader, cSeqHeader, fromHeader, toHeader, HeaderBuilder.createViaHeaders(extension.getTransport()), maxForwards);
			ContactHeader contactHeader = HeaderBuilder.createContactHeader(extension);
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
			ToHeader toHeader = HeaderBuilder.createToHeader(toCallParam.getExtension());
			SipURI requestURI = HeaderBuilder.createSipUri(toCallParam.getExtension());
			ArrayList<ViaHeader> viaHeaders = HeaderBuilder.createViaHeaders(toCallParam.getExtension().getTransport());
			CallIdHeader callIdHeader = HeaderBuilder.createCallIdHeader();
			CSeqHeader cSeqHeader = HeaderBuilder.createCseqHeader(toCallParam.getExtension().getTransport(), Request.INVITE);
			MaxForwardsHeader maxForwards = HeaderBuilder.createMaxForwardsHeader(70, toCallParam.getExtension().getTransport());
			Request request = toCallParam.getExtension().getTransport().getMessageFactory().createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			ContactHeader contactHeader = HeaderBuilder.createContactHeader(toCallParam.getExtension());
			request.addHeader(contactHeader);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
