package sipserver.com.core.sip.builder;

import java.util.ArrayList;
import java.util.Objects;

import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;

import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import sipserver.com.domain.Extension;
import sipserver.com.executer.starter.ServerCore;
import sipserver.com.util.GeneratorUtil;

public class HeaderBuilder {

	public static Address createAddress(Extension extension) throws Exception {
		SipURI addressUri = ServerCore.getCoreElement().getAddressFactory().createSipURI(extension.getExten(), extension.getAddress());
		Address address = ServerCore.getCoreElement().getAddressFactory().createAddress(addressUri);
		if (Objects.nonNull(extension.getDisplayName())) {
			address.setDisplayName(extension.getDisplayName());
		}
		return address;
	}

	public static SipURI createSipUri(Extension extension) {
		try {
			String serverHostPort = ServerCore.getCoreElement().getLocalServerAddress() + ":" + ServerCore.getCoreElement().getLocalSipPort();
			return ServerCore.getCoreElement().getAddressFactory().createSipURI(extension.getExten(), serverHostPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<ViaHeader> createViaHeaders(boolean isWs) {
		try {
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			ViaHeader viaHeader = ServerCore.getCoreElement().getHeaderFactory().createViaHeader(ServerCore.getCoreElement().getLocalServerAddress(), ServerCore.getCoreElement().getLocalSipPort(), isWs ? "WS" : "UDP", GeneratorUtil.getUUidForBranch());
			viaHeaders.add(viaHeader);
			return viaHeaders;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static RouteHeader createRouteHeader(Extension extension) {
		try {
			return ServerCore.getCoreElement().getHeaderFactory().createRouteHeader(ServerCore.getCoreElement().getAddressFactory().createAddress(createSipUri(extension)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static FromHeader createFromHeader(Extension extension) {
		try {
			return ServerCore.getCoreElement().getHeaderFactory().createFromHeader(createAddress(extension), GeneratorUtil.getUUidForTag());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ToHeader createToHeader(Extension extension) {
		try {
			return ServerCore.getCoreElement().getHeaderFactory().createToHeader(createAddress(extension), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static CallIdHeader createCallIdHeader() {
		try {
			return new CallID(GeneratorUtil.getUUid(10));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static CSeqHeader createCseqHeader(String method) {
		try {
			return ServerCore.getCoreElement().getHeaderFactory().createCSeqHeader(1L, method);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static MaxForwardsHeader createMaxForwardsHeader(int i) {
		try {
			return ServerCore.getCoreElement().getHeaderFactory().createMaxForwardsHeader(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ContactHeader createContactHeader(Extension extension) {
		try {
			Address contactAddress = ServerCore.getCoreElement().getAddressFactory().createAddress(createSipUri(extension));
			if (Objects.nonNull(extension.getDisplayName())) {
				contactAddress.setDisplayName(extension.getDisplayName());
			}
			return ServerCore.getCoreElement().getHeaderFactory().createContactHeader(contactAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getBranch(Message message) {
		if (message == null) {
			return null;
		}
		ViaHeader viaHeader = (ViaHeader) message.getHeader(ViaHeader.NAME);
		if (viaHeader == null) {
			return null;
		}
		if (viaHeader.getBranch() == null || viaHeader.getBranch().length() == 0) {
			return null;
		}
		return viaHeader.getBranch();
	}

	public static boolean isHaveAuthenticateHeader(Message message) {

		if (Objects.nonNull(message.getHeader(WWWAuthenticate.NAME))) {
			return true;
		}
		if (Objects.nonNull(message.getHeader(PAssertedIdentityHeader.NAME))) {
			return true;
		}
		if (Objects.nonNull(message.getHeader(ProxyAuthenticateHeader.NAME))) {
			return true;
		}
		if (Objects.nonNull(message.getHeader(ProxyAuthorizationHeader.NAME))) {
			return true;
		}
		return false;
	}

	public static String getSdp(String message) {
		if (!message.contains("v=0")) {
			return null;
		}
		String[] lines = message.split("\n");
		if (lines == null || lines.length <= 0) {
			return null;
		}

		String sdp = "";
		boolean sdpBegin = false;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("v=0")) {
				sdpBegin = true;
			}
			if (sdpBegin) {
				sdp += lines[i];
			}
		}
		return sdp;
	}

}
