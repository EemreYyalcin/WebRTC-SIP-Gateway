package sipserver.com.executer.sip.options;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.server.SipServerTransport;

public class OptionsClientTransaction extends ClientTransaction {

	public OptionsClientTransaction(Request request, InetAddress address, int port, SipServerTransport transport) {
		super(request, address, port, transport);
	}

	@Override
	public void processResponse(Response response) {
		try {
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			Objects.requireNonNull(toHeader);
			Extension extension = Extension.getExtension(toHeader);
			Objects.requireNonNull(extension);
			extension.setAlive(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
