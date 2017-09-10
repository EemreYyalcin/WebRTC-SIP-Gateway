package sipserver.com.service.invite;

import java.util.Properties;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.CreateConnectionResponse;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;

public class InviteServiceIn extends Service {

	private static StackLogger logger = CommonLogger.getLogger(InviteServiceIn.class);

	private Properties channelList = new Properties();

	public InviteServiceIn() {
		super(logger);
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport) throws Exception {
		String message = requestEvent.getRequest().toString();
		ServerTransaction serverTransaction = transport.getSipProvider().getNewServerTransaction(requestEvent.getRequest());
		try {
//			logger.logFatalError("RegisterRequestProcess:\r\n" + message);
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
			channelParameter.setExtension(extension);
			channelParameter.setServerTransaction(serverTransaction);
			putChannel(callIDHeader.getCallId(), channelParameter);
			// TODO: RouteService
			ServerCore.getServerCore().getIvrEndpointService().createConnection(callIDHeader.getCallId(), this, requestEvent.getRequest().getRawContent());

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

	@Override
	public void beginTask(String taskId, int timeout, Object exten) {
	}

	@Override
	public void endTask(String taskId) {
	}

	public ChannelParameter getChannel(String id) {
		return (ChannelParameter) channelList.get(id);
	}

	public ChannelParameter takeChannel(String id) {
		ChannelParameter channelParameter = (ChannelParameter) channelList.get(id);
		channelList.remove(id);
		return channelParameter;
	}

	public void putChannel(String key, ChannelParameter channelParameter) {
		channelList.put(key, channelParameter);
	}

	@Override
	public void mediaServerEvents(JainMgcpResponseEvent jainmgcpresponseevent, String callID) {
		ChannelParameter channelParameter = (ChannelParameter) takeChannel(callID);
		if (channelParameter == null) {
			logger.logFatalError("Transaction Does Not Exist!! Look At Me !!!");
			return;
		}
		CreateConnectionResponse responseConnection = (CreateConnectionResponse) jainmgcpresponseevent;
		String localSDP = getSdp(responseConnection.toString());
		logger.logFatalError("DDEBUG 1");
		Response response = createResponseMessage(channelParameter.getServerTransaction().getRequest(), Response.OK, localSDP);
		logger.logFatalError("DDEBUG 2");
		addContactHeader(response, channelParameter.getExtension());
		ServerCore.getServerCore().getTransportService().sendResponseMessage(channelParameter.getServerTransaction(), response);

	}

}
