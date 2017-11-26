package sipserver.com.executer.sip.invite;

import java.util.Objects;

import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.operational.RouteService;

public class InviteServerTransaction extends ServerTransaction {

	public InviteServerTransaction(Extension extension) {
		super(extension);
	}

	@Override
	public void processRequest() {
		try {
			sendResponseMessage(Response.TRYING);
			FromHeader fromHeader = (FromHeader) getRequest().getHeader(FromHeader.NAME);
			Objects.requireNonNull(fromHeader);

			ToHeader toHeader = (ToHeader) getRequest().getHeader(ToHeader.NAME);
			Objects.requireNonNull(toHeader);

			CallParam fromCallParam = new CallParam();
			fromCallParam.setExtension(getExtension()).setRequest(getRequest());
			if (Objects.nonNull(getRequest().getRawContent())) {
				fromCallParam.setSdpRemoteContent(new String(getRequest().getRawContent()));
			}
			setCallParam(fromCallParam);
			RouteService.route(this, toHeader);

		} catch (Exception e) {
			e.printStackTrace();
			sendResponseMessage(Response.BAD_EVENT);
			error("Invite Server Transaction Message Error! " + e.getMessage());
		}
	}

	@Override
	public void processACK() {
		super.processACK();

	}
}
