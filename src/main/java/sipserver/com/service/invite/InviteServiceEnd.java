package sipserver.com.service.invite;

import javax.sip.DialogTerminatedEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.service.util.message.CreateMessageService;

public class InviteServiceEnd extends Service {

	private static StackLogger logger = CommonLogger.getLogger(InviteServiceEnd.class);

	public InviteServiceEnd() {
		super(logger);
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport, ServerTransaction serverTransaction) throws Exception {
		try {
//			CallIdHeader callIDHeader = (CallIdHeader) requestEvent.getRequest().getHeader(CallIdHeader.NAME);
//			if (callIDHeader == null) {
//				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT, null);
//				return;
//			}
//
//			ViaHeader viaHeader = (ViaHeader) requestEvent.getRequest().getHeader(ViaHeader.NAME);
//			if (viaHeader == null) {
//				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT, null);
//				return;
//			}
//
//			if (viaHeader.getBranch() == null || viaHeader.getBranch().length() == 0) {
//				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.NOT_ACCEPTABLE_HERE, null);
//				return;
//			}
//
//			if (getChannel(viaHeader.getBranch()) != null) {
//				return;
//			}
//
//			FromHeader fromHeader = (FromHeader) requestEvent.getRequest().getHeader(FromHeader.NAME);
//			if (fromHeader == null) {
//				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT, null);
//				return;
//			}
//
//			ToHeader toHeader = (ToHeader) requestEvent.getRequest().getHeader(ToHeader.NAME);
//			if (toHeader == null) {
//				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT, null);
//				return;
//			}
//
//			Extension extension = CreateMessageService.createExtension(fromHeader);
//			if (extension == null) {
//				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.FORBIDDEN, null);
//				return;
//			}
//
//			CallParam callParam = ServerCore.getServerCore().takeChannel(callIDHeader.getCallId());
//			if (callParam == null) {
//				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, null);
//				return;
//			}
//			ServerCore.getServerCore().getStatusService().bye(callParam);
//			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.OK, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processTimeout(TimeoutEvent timeoutEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent terminatedEvent) {
		// TODO Auto-generated method stub

	}

}
