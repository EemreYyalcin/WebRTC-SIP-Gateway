package sipserver.com.service.util.message;

import java.util.ArrayList;
import java.util.Objects;

import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;

import gov.nist.javax.sip.header.CallID;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.util.GeneraterService;

public class HeaderBuilder {

	public static Address createAddress(Extension extension) throws Exception {
		SipURI addressUri = extension.getTransport().getAddressFactory().createSipURI(extension.getExten(), extension.getAddress().getHostAddress());
		Address address = extension.getTransport().getAddressFactory().createAddress(addressUri);
		if (Objects.nonNull(extension.getDisplayName())) {
			address.setDisplayName(extension.getDisplayName());
		}
		return address;
	}

	public static SipURI createSipUri(Extension extension) {
		try {
			String serverHostPort = ServerCore.getCoreElement().getLocalServerAddress().getHostAddress() + ":" + ServerCore.getCoreElement().getLocalSipPort();
			return extension.getTransport().getAddressFactory().createSipURI(extension.getExten(), serverHostPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<ViaHeader> createViaHeaders(SipServerTransport transport) {
		try {
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			ViaHeader viaHeader = transport.getHeaderFactory().createViaHeader(ServerCore.getCoreElement().getLocalServerAddress().getHostAddress(), ServerCore.getCoreElement().getLocalSipPort(), "UDP", GeneraterService.getUUidForBranch());
			viaHeaders.add(viaHeader);
			return viaHeaders;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static RouteHeader createRouteHeader(Extension extension) {
		try {
			return extension.getTransport().getHeaderFactory().createRouteHeader(extension.getTransport().getAddressFactory().createAddress(createSipUri(extension)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static FromHeader createFromHeader(Extension extension) {
		try {
			return extension.getTransport().getHeaderFactory().createFromHeader(createAddress(extension), GeneraterService.getUUidForTag());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ToHeader createToHeader(Extension extension) {
		try {
			return extension.getTransport().getHeaderFactory().createToHeader(createAddress(extension), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static CallIdHeader createCallIdHeader() {
		try {
			return new CallID(GeneraterService.getUUid(10));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static CSeqHeader createCseqHeader(SipServerTransport transport, String method) {
		try {
			return transport.getHeaderFactory().createCSeqHeader(1L, method);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static MaxForwardsHeader createMaxForwardsHeader(int i, SipServerTransport transport) {
		try {
			return transport.getHeaderFactory().createMaxForwardsHeader(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ContactHeader createContactHeader(Extension extension) {
		try {
			Address contactAddress = extension.getTransport().getAddressFactory().createAddress(createSipUri(extension));
			if (Objects.nonNull(extension.getDisplayName())) {
				contactAddress.setDisplayName(extension.getDisplayName());
			}
			return extension.getTransport().getHeaderFactory().createContactHeader(contactAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
