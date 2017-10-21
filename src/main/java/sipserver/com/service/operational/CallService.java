package sipserver.com.service.operational;

import javax.sip.message.Request;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.TransactionBuilder;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.control.ChannelControlService;
import sipserver.com.util.log.LogTest;

public class CallService {

	public static void beginCall(CallParam fromCallParam, Extension toExten) {
		try {
			if (!toExten.isRegister()) {
				BridgeService.noRoute(fromCallParam);
				LogTest.log(fromCallParam, "Not Route 3");
				return;
			}

			// if (!toExten.isAlive()) {
			// ServerCore.getServerCore().getStatusService().noRoute(fromCallParam);
			// LogTest.log(fromCallParam, "Not Route 4");
			// return;
			// }
			CallParam toCallParam = new CallParam();
			toCallParam.setExtension(toExten);
			fromCallParam.setBridgeCallParam(toCallParam);
			toCallParam.setBridgeCallParam(fromCallParam);
			// TODO: CreateConnection Mgcp Command Set toCallParam sdpLocalContent
			toCallParam.setSdpLocalContent(fromCallParam.getSdpRemoteContent());

			Request request = ClientTransaction.createInviteMessage(toCallParam);
			request.setContent(toCallParam.getSdpLocalContent(), toExten.getTransport().getHeaderFactory().createContentTypeHeader("application", "sdp"));
			ClientTransaction clientTransaction = TransactionBuilder.createAndStartClientTransaction(request, toExten.getAddress(), toExten.getPort(), toExten.getTransport());
			toCallParam.setTransaction(clientTransaction).setRequest(request);
			ChannelControlService.putChannel(clientTransaction.getCallId(), toCallParam);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
