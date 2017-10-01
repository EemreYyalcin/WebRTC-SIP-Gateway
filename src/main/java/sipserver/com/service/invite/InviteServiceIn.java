package sipserver.com.service.invite;

import javax.sip.DialogTerminatedEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.service.util.ExceptionService;
import sipserver.com.service.util.message.CreateMessageService;
import sipserver.com.util.log.LogTest;

public class InviteServiceIn extends Service {

	private static StackLogger logger = CommonLogger.getLogger(InviteServiceIn.class);

	public InviteServiceIn() {
		super(logger);
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport, ServerTransaction serverTransaction) throws Exception {
		try {
			if (requestEvent.getRequest().getMethod().equals(Request.CANCEL)) {
				processCancelMessage(requestEvent, transport);
				return;
			}
			// logger.logFatalError("RegisterRequestProcess:\r\n" + message);
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.TRYING, null);

			FromHeader fromHeader = (FromHeader) requestEvent.getRequest().getHeader(FromHeader.NAME);
			ExceptionService.checkNullObject(fromHeader);

			ToHeader toHeader = (ToHeader) requestEvent.getRequest().getHeader(ToHeader.NAME);
			ExceptionService.checkNullObject(toHeader);

			CSeqHeader cseqHeader = (CSeqHeader) requestEvent.getRequest().getHeader(CSeqHeader.NAME);
			ExceptionService.checkNullObject(cseqHeader);

			Extension fromExtension = CreateMessageService.createExtension(fromHeader);
			if (fromExtension == null) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.FORBIDDEN, null);
				return;
			}

			CallParam fromCallParam = new CallParam();
			fromCallParam.setExtension(fromExtension).setTransaction(serverTransaction).setRequest(requestEvent.getRequest());

			if (requestEvent.getRequest().getRawContent() != null) {
				fromCallParam.setSdpRemoteContent(new String(requestEvent.getRequest().getRawContent()));
			}
			ServerCore.getServerCore().getChannelControlService().putChannel(fromExtension.getExten(), fromCallParam);
			LogTest.log(this, "DDDEBUG");
			ServerCore.getServerCore().getRouteService().route(fromCallParam, toHeader);
		} catch (Exception e) {
			e.printStackTrace();
			logger.logFatalError("Message Error. Message:" + requestEvent.getRequest().toString());
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT, null);
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		// NON
	}

	private void processCancelMessage(RequestEvent requestEvent, SipServerTransport transport) throws Exception {
		// try {
		// ServerTransaction serverTransaction = requestEvent.getServerTransaction();
		// if (serverTransaction == null) {
		// serverTransaction = getServerTransaction(transport.getSipProvider(),
		// requestEvent.getRequest());
		// ExceptionService.checkNullObject(serverTransaction);
		// }
		// String branch = AliasService.getBranch(requestEvent.getRequest());
		// CallParam callParam = takeChannel(branch);
		// if (callParam == null) {
		// ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction,
		// requestEvent.getRequest(), Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST,
		// null);
		// return;
		// }
		// callParam.setSecondrequest(requestEvent.getRequest());
		// ServerCore.getServerCore().getBridgeService().cancel(callParam);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processTimeout(TimeoutEvent timeoutEvent) {
		LogTest.log(this, "InviteIn timeout");
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent terminatedEvent) {
		LogTest.log(this, "InviteIn Terminated");
	}

}
