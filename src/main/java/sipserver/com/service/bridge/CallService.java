package sipserver.com.service.bridge;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;

public class CallService {

	public void bridgeCall(CallParam fromCallParam, Extension toExten) {
		try {
			if (fromCallParam.getExtension() == null || toExten == null) {
				System.out.println("ChannelParameter Error");
				throw new Exception();
			}
			if (fromCallParam.getTransaction() == null) {
				System.out.println("ChannelParameter Error Transaction Error!");
				throw new Exception();
			}
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
