package sipserver.com.service.route;

import javax.sip.header.ToHeader;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.util.message.CreateMessageService;
import sipserver.com.util.log.LogTest;

public class RouteService {

	public void route(CallParam fromCallParam, ToHeader toHeader) {
		try {
			if (fromCallParam.getTransaction() == null) {
				System.out.println("ChannelParameter Error Transaction Error!");
				throw new Exception();
			}

			// TODO: Routing Service
			Extension toExtenFromHeader = CreateMessageService.createExtension(toHeader);
			if (toExtenFromHeader == null) {
				ServerCore.getServerCore().getBridgeService().noRoute(fromCallParam);
				LogTest.log(fromCallParam, "Not Route 1");
				return;
			}
			Extension localExtension = ServerCore.getServerCore().getLocalExtension(toExtenFromHeader.getExten());
			if (localExtension == null) {
				ServerCore.getServerCore().getBridgeService().noRoute(fromCallParam);
				LogTest.log(fromCallParam, "Not Route 2");
				return;
			}
			ServerCore.getServerCore().getCallService().bridgeCall(fromCallParam, localExtension);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
