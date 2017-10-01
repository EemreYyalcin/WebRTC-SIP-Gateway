package sipserver.com.service.util.message;

import javax.sip.address.Address;
import javax.sip.address.SipURI;

import sipserver.com.server.SipServerTransport;

public class CreateHeaderService {

	public static Address createAddress(SipServerTransport transport, String exten, String host, String displayName) throws Exception {
		SipURI addressUri = transport.getAddressFactory().createSipURI(exten, host);
		Address address = transport.getAddressFactory().createAddress(addressUri);
		if (displayName != null) {
			address.setDisplayName(displayName);
		}
		return address;
	}

}
