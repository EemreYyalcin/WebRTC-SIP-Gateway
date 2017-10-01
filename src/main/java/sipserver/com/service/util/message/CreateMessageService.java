package sipserver.com.service.util.message;

import java.util.ArrayList;

import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderAddress;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import gov.nist.javax.sip.header.CallID;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.util.ExceptionService;
import sipserver.com.service.util.GeneraterService;

public class CreateMessageService {

	public static Extension createExtension(HeaderAddress headerAddress) {
		try {
			Extension extension = new Extension();
			String uri = headerAddress.getAddress().getURI().toString().trim();
			String scheme = headerAddress.getAddress().getURI().getScheme();
			String[] parts = uri.split(";");
			if (parts.length <= 0) {
				throw new Exception();
			}
			uri = parts[0].substring(scheme.length() + 1);
			if (uri.indexOf(":") > 0) {
				uri = uri.split(":")[0];
			}

			if (uri.indexOf("@") < 0) {
				throw new Exception();
			}
			String[] userAndHost = uri.split("@");
			if (userAndHost.length < 2) {
				throw new Exception();
			}
			extension.setExten(userAndHost[0]);
			extension.setHost(userAndHost[1]);
			extension.setDisplayName(headerAddress.getAddress().getDisplayName());
			if (headerAddress instanceof ContactHeader) {
				int expiresTime = ((ContactHeader) headerAddress).getExpires();
				if (expiresTime != -1) {
					extension.setExpiresTime(expiresTime);
				}

			}
			return extension;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Request createInviteMessage(CallParam toCallParam) {
		try {

			SipServerTransport transport = ServerCore.getTransport(toCallParam.getExtension().getTransportType());
			ExceptionService.checkNullObject(transport);
			Address fromAddress = CreateHeaderService.createAddress(transport, toCallParam.getBridgeCallParam().getExtension().getExten(), toCallParam.getBridgeCallParam().getExtension().getHost(), toCallParam.getExtension().getDisplayName());
			FromHeader fromHeader = transport.getHeaderFactory().createFromHeader(fromAddress, GeneraterService.getUUidForTag());
			// create To Header
			Address toAddress = CreateHeaderService.createAddress(transport, toCallParam.getExtension().getExten(), toCallParam.getExtension().getHost(), toCallParam.getExtension().getDisplayName());
			ToHeader toHeader = transport.getHeaderFactory().createToHeader(toAddress, null);

			// create Request URI
			SipURI requestURI = transport.getAddressFactory().createSipURI(toCallParam.getExtension().getExten(), toCallParam.getExtension().getHost() + ":" + toCallParam.getExtension().getPort());
			// Create ViaHeaders

			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			ViaHeader viaHeader = transport.getHeaderFactory().createViaHeader(transport.getHost(), transport.getPort(), transport.getProtocol(), GeneraterService.getUUidForBranch());
			// add via headers
			viaHeaders.add(viaHeader);

			// SipURI sipuri =
			// transport.getAddressFactory().createSipURI(extension.getExten(),
			// extension.getHost());
			// sipuri.setPort(extension.getPort());
			// sipuri.setLrParam();
			//
			// RouteHeader routeHeader =
			// transport.getHeaderFactory().createRouteHeader(transport.getAddressFactory().createAddress(sipuri));

			// Create a new CallId header
			// CallIdHeader callIdHeader = sipProvider.getNewCallId();
			CallIdHeader callIdHeader = new CallID(GeneraterService.getUUid(10));

			// Create a new Cseq header
			CSeqHeader cSeqHeader = transport.getHeaderFactory().createCSeqHeader(1L, Request.INVITE);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = transport.getHeaderFactory().createMaxForwardsHeader(70);

			// Create the request.
			Request request = transport.getMessageFactory().createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			// Create contact headers
			SipURI contactUrl = transport.getAddressFactory().createSipURI(toCallParam.getBridgeCallParam().getExtension().getExten(), transport.getHost());
			contactUrl.setPort(transport.getPort());

			// Create the contact name address.
			SipURI contactURI = transport.getAddressFactory().createSipURI(toCallParam.getBridgeCallParam().getExtension().getExten(), transport.getHost());
			contactURI.setPort(transport.getPort());

			Address contactAddress = transport.getAddressFactory().createAddress(contactURI);

			if (toCallParam.getBridgeCallParam().getExtension().getDisplayName() != null) {
				// Add the contact address.
				contactAddress.setDisplayName(toCallParam.getBridgeCallParam().getExtension().getDisplayName());
			}

			ContactHeader contactHeader = transport.getHeaderFactory().createContactHeader(contactAddress);
			request.addHeader(contactHeader);
			// TODO: Dont use the Outbound Proxy. Use Lr instead. RouteHeader
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Request createOptionsMessage(Extension extension) {
		try {
			SipServerTransport transport = ServerCore.getTransport(extension.getTransportType());
			if (transport == null) {
				throw new Exception();
			}

			SipURI fromAddress = transport.getAddressFactory().createSipURI("", transport.getHost());

			Address fromNameAddress = transport.getAddressFactory().createAddress(fromAddress);
			FromHeader fromHeader = transport.getHeaderFactory().createFromHeader(fromNameAddress, GeneraterService.getUUidForTag());
			// create To Header
			SipURI toAddress = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			Address toNameAddress = transport.getAddressFactory().createAddress(toAddress);
			if (extension.getDisplayName() != null) {
				toNameAddress.setDisplayName(extension.getDisplayName());
			}
			ToHeader toHeader = transport.getHeaderFactory().createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost() + ":" + extension.getPort());

			// Create ViaHeaders

			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			ViaHeader viaHeader = transport.getHeaderFactory().createViaHeader(transport.getHost(), transport.getPort(), transport.getProtocol(), GeneraterService.getUUidForBranch());
			// add via headers
			viaHeaders.add(viaHeader);

			SipURI sipuri = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			sipuri.setPort(extension.getPort());
			sipuri.setLrParam();

			RouteHeader routeHeader = transport.getHeaderFactory().createRouteHeader(transport.getAddressFactory().createAddress(sipuri));

			CallIdHeader callIdHeader = new CallID(GeneraterService.getUUid(10));

			// Create a new Cseq header
			CSeqHeader cSeqHeader = transport.getHeaderFactory().createCSeqHeader(1L, Request.OPTIONS);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = transport.getHeaderFactory().createMaxForwardsHeader(70);

			// Create the request.
			Request request = transport.getMessageFactory().createRequest(requestURI, Request.OPTIONS, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			// Create contact headers

			SipURI contactUrl = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			contactUrl.setPort(transport.getPort());

			// Create the contact name address.
			SipURI contactURI = transport.getAddressFactory().createSipURI(extension.getExten(), transport.getHost());
			contactURI.setPort(transport.getPort());

			Address contactAddress = transport.getAddressFactory().createAddress(contactURI);

			if (extension.getDisplayName() != null) {
				// Add the contact address.
				contactAddress.setDisplayName(extension.getDisplayName());
			}

			ContactHeader contactHeader = transport.getHeaderFactory().createContactHeader(contactAddress);
			request.addHeader(contactHeader);
			// Dont use the Outbound Proxy. Use Lr instead.
			request.setHeader(routeHeader);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Request createRegisterMessage(Extension extension) {
		try {
			SipServerTransport transport = ServerCore.getTransport(extension.getTransportType());
			if (transport == null) {
				throw new Exception();
			}

			SipURI fromAddress = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());

			Address fromNameAddress = transport.getAddressFactory().createAddress(fromAddress);
			if (extension.getDisplayName() != null) {
				fromNameAddress.setDisplayName(extension.getDisplayName());
			}

			FromHeader fromHeader = transport.getHeaderFactory().createFromHeader(fromNameAddress, GeneraterService.getUUidForTag());
			// create To Header
			SipURI toAddress = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			Address toNameAddress = transport.getAddressFactory().createAddress(toAddress);
			if (extension.getDisplayName() != null) {
				toNameAddress.setDisplayName(extension.getDisplayName());
			}
			ToHeader toHeader = transport.getHeaderFactory().createToHeader(toNameAddress, null);

			// create Request URI
			String serverHostPort = ServerCore.getCoreElement().getLocalServerIp() + ":" + ServerCore.getCoreElement().getLocalSipPort();
			SipURI requestURI = transport.getAddressFactory().createSipURI(extension.getExten(), serverHostPort);

			// Create ViaHeaders

			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			ViaHeader viaHeader = transport.getHeaderFactory().createViaHeader(transport.getHost(), transport.getPort(), transport.getProtocol(), GeneraterService.getUUidForBranch());
			// add via headers
			viaHeaders.add(viaHeader);

			SipURI sipuri = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			sipuri.setPort(extension.getPort());
			sipuri.setLrParam();

			RouteHeader routeHeader = transport.getHeaderFactory().createRouteHeader(transport.getAddressFactory().createAddress(sipuri));

			// Create ContentTypeHeader
			// ContentTypeHeader contentTypeHeader =
			// ProtocolObjects.headerFactory.createContentTypeHeader("application",
			// "sdp");

			// Create a new CallId header
			// CallIdHeader callIdHeader = sipProvider.getNewCallId();
			CallIdHeader callIdHeader = new CallID(GeneraterService.getUUid(10));

			// Create a new Cseq header
			CSeqHeader cSeqHeader = transport.getHeaderFactory().createCSeqHeader(1L, Request.REGISTER);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = transport.getHeaderFactory().createMaxForwardsHeader(70);

			// Create the request.
			Request request = transport.getMessageFactory().createRequest(requestURI, Request.REGISTER, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			// Create contact headers

			SipURI contactUrl = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			contactUrl.setPort(transport.getPort());

			// Create the contact name address.
			SipURI contactURI = transport.getAddressFactory().createSipURI(extension.getExten(), transport.getHost());
			contactURI.setPort(transport.getPort());

			Address contactAddress = transport.getAddressFactory().createAddress(contactURI);

			if (extension.getDisplayName() != null) {
				// Add the contact address.
				contactAddress.setDisplayName(extension.getDisplayName());
			}

			ContactHeader contactHeader = transport.getHeaderFactory().createContactHeader(contactAddress);
			request.addHeader(contactHeader);
			// Dont use the Outbound Proxy. Use Lr instead.
			request.setHeader(routeHeader);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Request createCancelMessage() {
		// TODO: Create Cancel Message
		return null;
	}

	public static Request createByeMessage() {
		return null;
	}

}
