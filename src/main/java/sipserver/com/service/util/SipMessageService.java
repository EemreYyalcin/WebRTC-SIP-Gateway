package sipserver.com.service.util;

import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.message.Message;

public class SipMessageService {

	public static boolean isHaveAuthenticateHeader(Message message) {
		if (message.getHeader(WWWAuthenticate.NAME) != null) {
			return true;
		}
		if (message.getHeader(PAssertedIdentityHeader.NAME) != null) {
			return true;
		}
		if (message.getHeader(ProxyAuthenticateHeader.NAME) != null) {
			return true;
		}
		if (message.getHeader(ProxyAuthorizationHeader.NAME) != null) {
			return true;
		}
		return false;
	}
}
