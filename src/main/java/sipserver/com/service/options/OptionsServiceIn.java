package sipserver.com.service.options;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.service.util.CreateService;
import sipserver.com.util.log.LogTest;

public class OptionsServiceIn extends Service {

	private static StackLogger logger = CommonLogger.getLogger(OptionsServiceIn.class);

	public OptionsServiceIn() {
		super(logger);
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport) throws Exception {
		LogTest.log("Options 0 " + requestEvent.getRequest().getRequestURI());
		ServerTransaction serverTransaction = getServerTransaction(transport.getSipProvider(), requestEvent.getRequest());
		if (serverTransaction == null) {
			return;
		}
		try {
			CallIdHeader callIDHeader = (CallIdHeader) requestEvent.getRequest().getHeader(CallIdHeader.NAME);
			if (callIDHeader == null) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT, null);
				return;
			}

			ContactHeader contactHeader = (ContactHeader) requestEvent.getRequest().getHeader(ContactHeader.NAME);
			if (contactHeader == null) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.NOT_FOUND, null);
				logger.logFatalError("Contact Header is Null. Message");
				return;
			}

			Extension extIncoming = CreateService.createExtension(contactHeader);
			if (extIncoming == null) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.NOT_FOUND, null);
				logger.logFatalError("Contact Header is Null. Message");
				return;
			}
			extIncoming.setTransportType(transport);
			if (extIncoming == null || extIncoming.getExten() == null || extIncoming.getHost() == null) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.NOT_FOUND, null);
				return;
			}

			Extension extensionLocal = ServerCore.getServerCore().getLocalExtension(extIncoming.getExten());
			if (extensionLocal == null) {
				extensionLocal = ServerCore.getServerCore().getTrunkExtension(extIncoming.getExten());
				if (extensionLocal == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.NOT_FOUND, null);
					return;
				}
			}
			extensionLocal.setAlive(true);
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.OK, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		// TODO Auto-generated method stub

	}

}
