package sipserver.com.service.options;

import java.util.Objects;

import javax.sip.DialogTerminatedEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.service.util.ExceptionService;
import sipserver.com.service.util.message.CreateMessageService;

public class OptionsServiceIn extends Service {

	private static StackLogger logger = CommonLogger.getLogger(OptionsServiceIn.class);

	public OptionsServiceIn() {
		super(logger);
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport, ServerTransaction serverTransaction) throws Exception {
		try {
			CallIdHeader callIDHeader = (CallIdHeader) requestEvent.getRequest().getHeader(CallIdHeader.NAME);
			if (Objects.isNull(callIDHeader)) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT, null);
				return;
			}

			ContactHeader contactHeader = (ContactHeader) requestEvent.getRequest().getHeader(ContactHeader.NAME);
			ExceptionService.checkNullObject(contactHeader);

			Extension extIncoming = CreateMessageService.createExtension(contactHeader);
			ExceptionService.checkNullObject(extIncoming);
			extIncoming.setTransportType(transport);
			ExceptionService.checkNullObject(extIncoming.getExten());
			ExceptionService.checkNullObject(extIncoming.getHost());

			Extension extensionLocal = ServerCore.getCoreElement().getLocalExtension(extIncoming.getExten());
			if (Objects.isNull(extensionLocal)) {
				extensionLocal = ServerCore.getCoreElement().getTrunkExtension(extIncoming.getExten());
				ExceptionService.checkNullObject(extensionLocal);
			}
			extensionLocal.setAlive(true);
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.OK, null);
		} catch (Exception e) {
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.NOT_FOUND, null);
		}

	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent event) {
		// LogTest.log(this, "Options Diolog Terminated " +
		// event.getDialog().getCallId());

	}

	@Override
	public void processTimeout(TimeoutEvent timeoutEvent) {
		// LogTest.log(this, "Options Process Timeout " +
		// timeoutEvent.getServerTransaction().getRequest());
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent terminatedEvent) {
		// LogTest.log(this, "Options Transaction Terminated " +
		// terminatedEvent.getServerTransaction().getRequest());

	}
}