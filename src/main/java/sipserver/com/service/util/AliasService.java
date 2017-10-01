package sipserver.com.service.util;

import javax.sip.header.ViaHeader;
import javax.sip.message.Message;

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

}
