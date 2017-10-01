package sipserver.com.service.bridge;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.util.log.LogTest;

public class CallService {

	public void bridgeCall(CallParam fromCallParam, Extension toExten) {
		try {
			if (!toExten.isRegister()) {
				ServerCore.getServerCore().getBridgeService().noRoute(fromCallParam);
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
			ServerCore.getServerCore().getInviteServiceOut().beginCall(toCallParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
