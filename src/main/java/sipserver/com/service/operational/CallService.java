package sipserver.com.service.operational;

import java.util.Objects;

import javax.sip.message.Request;

import org.apache.log4j.Logger;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.TransactionBuilder;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.control.ChannelControlService;

public class CallService {

	private static Logger logger = Logger.getLogger(CallService.class);

	public static void beginCall(CallParam fromCallParam, Extension toExten) {
		try {
			CallParam toCallParam = new CallParam();
			toCallParam.setExtension(toExten);
			fromCallParam.setBridgeCallParam(toCallParam);
			toCallParam.setBridgeCallParam(fromCallParam);
			// TODO: CreateConnection Mgcp Command Set toCallParam sdpLocalContent
			toCallParam.setSdpLocalContent(fromCallParam.getSdpRemoteContent());

			if (!toExten.isRegister()) {
				BridgeService.noRoute(toCallParam);
				logger.debug("Not Route 3");
				return;
			}

			if (!toExten.isAlive()) {
				BridgeService.noRoute(toCallParam);
				logger.debug("Not Route 3 " + toExten.getExten());
				return;
			}

			Request request = ClientTransaction.createInviteMessage(toCallParam);
			request.setContent(toCallParam.getSdpLocalContent(), toExten.getTransport().getHeaderFactory().createContentTypeHeader("application", "sdp"));
			ClientTransaction clientTransaction = TransactionBuilder.createAndStartClientTransaction(request, toExten);
			if (Objects.isNull(clientTransaction)) {
				BridgeService.noRoute(fromCallParam);
				logger.debug("Not Route 3");
				return;
			}
			ChannelControlService.putChannel(clientTransaction.getCallId(), toCallParam);
			toCallParam.setTransaction(clientTransaction).setRequest(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
