package sipserver.com.server.mediaserver;

import java.util.Objects;

import javax.sip.message.Request;

import com.mgcp.message.command.commandLine.MGCPCommandLine.MGCPVerb;
import com.mgcp.message.response.MGCPResponse;
import com.mgcp.transport.MgcpSessionInterface;
import com.noyan.Base;
import com.noyan.util.NullUtil;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.executer.sip.transaction.TransactionBuilder;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.operational.BridgeService;

public class OutgoingCallMediaSession implements MgcpSessionInterface, Base {

	private CallParam toCallParam;
	private ServerTransaction serverTransaction;

	public OutgoingCallMediaSession(CallParam toCallParam, ServerTransaction serverTransaction) {
		this.toCallParam = toCallParam;
		this.serverTransaction = serverTransaction;
	}

	@Override
	public void processException(Exception exception) {
		exception.printStackTrace();
	}

	@Override
	public void onSuccess(MGCPResponse mgcpResponse, MGCPVerb verb) {
		try {
			if (NullUtil.isNotNull(mgcpResponse.getSdpInformation())) {
				toCallParam.setSdpLocalContent(mgcpResponse.getSdpInformation());
			}
			if (verb.equals(MGCPVerb.CRCX)) {
				Request request = ClientTransaction.createInviteMessage(toCallParam, serverTransaction.getCallParam());
				request.setContent(toCallParam.getSdpLocalContent(), ServerCore.getCoreElement().getHeaderFactory().createContentTypeHeader("application", "sdp"));
				ClientTransaction clientTransaction = TransactionBuilder.createClientTransaction(request, toCallParam.getExtension());
				if (Objects.isNull(clientTransaction)) {
					BridgeService.noRoute(serverTransaction);
					debug("Not Route 3");
					return;
				}
				toCallParam.setRequest(request);
				return;
			}

			if (verb.equals(MGCPVerb.MDCX)) {
				info("MDCX SDP " + mgcpResponse.getSdpInformation());
				toCallParam.setSdpLocalContent(mgcpResponse.getSdpInformation());
				BridgeService.ok(serverTransaction, serverTransaction.getCallParam().getSdpLocalContent());
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(MGCPResponse mgcpResponse, MGCPVerb verb) {
		debug("MediaServer On Error !! " + verb);
		toCallParam.setError(true);
	}
}
