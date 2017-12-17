package sipserver.com.util;

import java.util.Objects;

import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;

import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;

public class AliasService {

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
