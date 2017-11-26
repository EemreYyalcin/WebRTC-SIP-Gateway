package sipserver.com.service.operational;

import java.util.Objects;

import javax.sip.header.ToHeader;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.executer.sip.transaction.ServerTransaction;

public class RouteService {

	private static Logger logger = Logger.getLogger(RouteService.class);

	public static void route(ServerTransaction serverTransaction, ToHeader toHeader) {
		try {
			// TODO: Routing Service
			Extension toExtenFromHeaderExtension = ExtensionBuilder.getExtension(toHeader);
			if (Objects.isNull(toExtenFromHeaderExtension)) {
				logger.trace("Route Ivr Service");
				if (SipServerSharedProperties.mediaServerActive) {
					IvrService.beginIvrCall(serverTransaction);
					return;
				}
				serverTransaction.sendResponseMessage(Response.BUSY_HERE);
				return;
			}

			CallService.bridgeCall(serverTransaction, toExtenFromHeaderExtension);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
