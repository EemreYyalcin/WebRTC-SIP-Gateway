package sipserver.com.executer.sip.options;

import java.util.Objects;

import javax.sip.header.ContactHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.sip.transaction.ServerTransaction;

public class OptionsServerTransaction extends ServerTransaction {

	@Override
	public void processRequest() {
		try {
			ContactHeader contactHeader = (ContactHeader) getRequest().getHeader(ContactHeader.NAME);
			Objects.requireNonNull(contactHeader);
			Extension extIncoming = ExtensionBuilder.getExtension(contactHeader, (ViaHeader) getRequest().getHeader(ViaHeader.NAME));
			if (Objects.isNull(extIncoming)) {
				warn("Peer has not Register. " + contactHeader.toString());
				return;
			}
			setExtension(extIncoming);
			extIncoming.setTransport(getTransport());
			debug("Keep Alive Exten:" + extIncoming.getExten());
			sendResponseMessage(Response.OK);
		} catch (Exception e) {
			e.printStackTrace();
			sendResponseMessage(Response.BAD_EVENT);
		}
	}
}
