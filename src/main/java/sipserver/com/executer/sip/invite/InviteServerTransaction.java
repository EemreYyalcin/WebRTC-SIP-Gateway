package sipserver.com.executer.sip.invite;

import java.util.Objects;

import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.operational.RouteService;

public class InviteServerTransaction extends ServerTransaction {

	@Override
	public void processRequest() {
		try {
			sendResponseMessage(Response.TRYING);
			ContactHeader contactHeader = (ContactHeader) getRequest().getHeader(ContactHeader.NAME);
			if (Objects.isNull(contactHeader)) {
				sendResponseMessage(Response.UNAUTHORIZED);
				error("Contact Header is Undefined");
				if (!getLogger().isTraceEnabled()) {
					error("Contact Header Error: " + getRequest().toString());
				}
				return;
			}

			FromHeader fromHeader = (FromHeader) getRequest().getHeader(FromHeader.NAME);
			Objects.requireNonNull(fromHeader);

			ToHeader toHeader = (ToHeader) getRequest().getHeader(ToHeader.NAME);
			Objects.requireNonNull(toHeader);

			Extension fromExtension = ExtensionBuilder.getExtension(fromHeader, (ViaHeader) getRequest().getHeader(ViaHeader.NAME));
			if (Objects.isNull(fromExtension)) {
				sendResponseMessage(Response.FORBIDDEN);
				info("Undefined Peer " + fromHeader.toString());
				return;
			}
			setExtension(fromExtension);
			CallParam fromCallParam = new CallParam();
			fromCallParam.setExtension(fromExtension).setTransaction(this).setRequest(getRequest());

			if (Objects.nonNull(getRequest().getRawContent())) {
				fromCallParam.setSdpRemoteContent(new String(getRequest().getRawContent()));
			}

			RouteService.route(fromCallParam, toHeader);

		} catch (Exception e) {
			e.printStackTrace();
			sendResponseMessage(Response.BAD_EVENT);
			error("Invite Server Transaction Message Error! " + e.getMessage());
		}
	}
}
