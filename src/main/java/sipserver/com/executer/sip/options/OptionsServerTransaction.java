package sipserver.com.executer.sip.options;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.ContactHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.server.SipServerTransport;

public class OptionsServerTransaction extends ServerTransaction {

	public OptionsServerTransaction(Request request, InetAddress address, int port, SipServerTransport transport) {
		super(request, address, port, transport);
	}

	@Override
	public void processRequest() {
		try {
			ContactHeader contactHeader = (ContactHeader) getRequest().getHeader(ContactHeader.NAME);
			Objects.requireNonNull(contactHeader);
			Extension extIncoming = Extension.getExtension(contactHeader);
			if (Objects.isNull(extIncoming)) {
				warn("Peer has not Register. " + contactHeader.toString());
				return;
			}
			extIncoming.setTransport(getTransport());
			extIncoming.setAlive(true);
			debug("Keep Alive Exten:" + extIncoming.getExten());
			sendResponseMessage(Response.OK);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponseMessage(Response.BAD_EVENT);
		}
	}

}
