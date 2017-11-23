package sipserver.com.server.mediaserver;

import java.util.Objects;

import com.mgcp.message.command.commandLine.MGCPCommandLine.MGCPVerb;
import com.mgcp.message.response.MGCPResponse;
import com.mgcp.transport.MgcpSessionInterface;
import com.noyan.Base;

import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.operational.CallService;

public class IncomingCallMediaSession implements MgcpSessionInterface, Base {

	private ServerTransaction serverTransaction;
	private CallParam toCallParam;

	public IncomingCallMediaSession(ServerTransaction serverTransaction, CallParam toCallParam) {
		this.serverTransaction = serverTransaction;
		this.toCallParam = toCallParam;
	}

	@Override
	public void processException(Exception exception) {
		exception.printStackTrace();
	}

	@Override
	public void onSuccess(MGCPResponse mgcpResponse, MGCPVerb verb) {
		debug("IN Success ");
		debug("SDP: " + mgcpResponse.getSdpInformation());
		if (verb.equals(MGCPVerb.RQNT)) {
			error("RQNT Response Taken");
			return;
		}

		if (Objects.isNull(mgcpResponse.getSdpInformation())) {
			return;
		}
		if (verb.equals(MGCPVerb.CRCX)) {
			serverTransaction.getCallParam().setSdpLocalContent(mgcpResponse.getSdpInformation());
			CallService.beginCall(toCallParam, serverTransaction);
			return;
		}
	}

	@Override
	public void onError(MGCPResponse mgcpResponse, MGCPVerb verb) {
		debug("MediaServer On Error !!");
	}

}
