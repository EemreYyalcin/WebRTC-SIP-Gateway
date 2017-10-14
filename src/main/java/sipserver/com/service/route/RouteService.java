package sipserver.com.service.route;

import java.util.Objects;

import javax.sip.header.ToHeader;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.util.message.CreateMessageService;
import sipserver.com.util.log.LogTest;

public class RouteService {

	public void route(CallParam fromCallParam, ToHeader toHeader) {
		try {
			Objects.requireNonNull(fromCallParam.getTransaction());

			// TODO: Routing Service
			Extension toExtenFromHeader = CreateMessageService.createExtension(toHeader);
			if (Objects.isNull(toExtenFromHeader)) {
				ServerCore.getServerCore().getBridgeService().noRoute(fromCallParam);
				LogTest.log(fromCallParam, "Not Route 1");
				return;
			}
			Extension localExtension = ServerCore.getCoreElement().getLocalExtension(toExtenFromHeader.getExten());
			if (Objects.isNull(localExtension)) {
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
