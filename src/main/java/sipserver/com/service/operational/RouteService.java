package sipserver.com.service.operational;

import java.util.Objects;

import javax.sip.header.ToHeader;

import org.apache.log4j.Logger;

import sipserver.com.domain.Extension;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.service.control.ChannelControlService;

public class RouteService {

	private static Logger logger = Logger.getLogger(RouteService.class);

	public static void route(CallParam fromCallParam, ToHeader toHeader) {
		try {
			// TODO: Routing Service
			Extension toExtenFromHeader = Extension.getExtension(toHeader);
			if (Objects.isNull(toExtenFromHeader)) {
				BridgeService.noRoute(fromCallParam);
				logger.debug("Not Route 1");
				return;
			}
			// TODO: Bridge Or IVR
			ChannelControlService.putChannel(fromCallParam.getTransaction().getCallId(), fromCallParam);
			CallService.beginCall(fromCallParam, toExtenFromHeader);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
