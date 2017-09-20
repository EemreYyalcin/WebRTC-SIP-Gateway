package sipserver.com.service.invite;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.service.param.ChannelParameter;

public class InviteServiceIn extends Service {

	private static StackLogger logger = CommonLogger.getLogger(InviteServiceIn.class);

	public InviteServiceIn() {
		super(logger);
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport) throws Exception {
		String message = requestEvent.getRequest().toString();
		ServerTransaction serverTransaction = transport.getSipProvider().getNewServerTransaction(requestEvent.getRequest());
		try {
			// logger.logFatalError("RegisterRequestProcess:\r\n" + message);
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.TRYING, null));

			CallIdHeader callIDHeader = (CallIdHeader) requestEvent.getRequest().getHeader(CallIdHeader.NAME);
			if (callIDHeader == null) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.BAD_EVENT, null));
				return;
			}

			if (getChannel(callIDHeader.getCallId()) != null) {
				return;
			}

			FromHeader fromHeader = (FromHeader) requestEvent.getRequest().getHeader(FromHeader.NAME);
			if (fromHeader == null) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.BAD_EVENT, null));
				return;
			}

			ToHeader toHeader = (ToHeader) requestEvent.getRequest().getHeader(ToHeader.NAME);
			if (toHeader == null) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.BAD_EVENT, null));
				return;
			}
			Extension extension = new Extension(fromHeader);
			ChannelParameter channelParameter = new ChannelParameter();
			channelParameter.setFromExtension(extension);
			channelParameter.setFromTransaction(serverTransaction);
			Extension toExten = new Extension(toHeader);
			channelParameter.setToExtension(ServerCore.getServerCore().getLocalExtension(toExten.getExten()));
			putChannel(callIDHeader.getCallId(), channelParameter);
			System.out.println("Incoming callId: " + callIDHeader.getCallId() );
			// TODO: RouteService
			ServerCore.getServerCore().getInviteServiceOut().beginCall(channelParameter);

		} catch (Exception e) {
			e.printStackTrace();
			logger.logFatalError("Message Error. Message:" + message);
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.BAD_EVENT, null));
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		// NON
	}

}
