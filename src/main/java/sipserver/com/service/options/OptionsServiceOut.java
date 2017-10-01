package sipserver.com.service.options;

import java.util.UUID;

import javax.sip.DialogTerminatedEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.service.util.ExceptionService;
import sipserver.com.service.util.message.CreateMessageService;

public class OptionsServiceOut extends Service {

	private static StackLogger logger = CommonLogger.getLogger(OptionsServiceOut.class);

	public OptionsServiceOut() {
		super(logger);
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport, ServerTransaction serverTransaction) throws Exception {
		// NON
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		try {
			if (responseEvent.getClientTransaction() == null) {
				throw new Exception();
			}

			ToHeader toHeader = (ToHeader) responseEvent.getResponse().getHeader(ToHeader.NAME);
			ExceptionService.checkNullObject(toHeader);
			Extension trunkExtension = CreateMessageService.createExtension(toHeader);
			ExceptionService.checkNullObject(trunkExtension);
			int statusCode = responseEvent.getResponse().getStatusCode();
			ServerCore.getServerCore().getTrunkExtension(trunkExtension.getExten()).getExtensionParameter().setRegisterResponseRecieved(true);
			ServerCore.getServerCore().getTrunkExtension(trunkExtension.getExten()).getExtensionParameter().setOptionsResponseCode(statusCode);

			if (lockProperties.get(trunkExtension.getExten()) != null) {
				synchronized (lockProperties.get(trunkExtension.getExten())) {
					lockProperties.get(trunkExtension.getExten()).notify();
				}
				lockProperties.remove(trunkExtension.getExten());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void ping(Extension extTrunk) {
		try {
			Request requestMessage = CreateMessageService.createOptionsMessage(extTrunk);
			SipServerTransport transport = ServerCore.getTransport(requestMessage);
			ExceptionService.checkNullObject(transport);
			extTrunk.getExtensionParameter().setOptionsResponseRecieved(false);
			ServerCore.getServerCore().getTransportService().sendRequestMessage(transport.getSipProvider().getNewClientTransaction(requestMessage));
			String lockValue = UUID.randomUUID().toString();
			lockProperties.put(extTrunk.getExten(), lockValue);
			synchronized (lockValue) {
				lockValue.wait(SipServerSharedProperties.messageTimeout);
			}
			if (!extTrunk.getExtensionParameter().isOptionsResponseRecieved()) {
				extTrunk.setAlive(false);
				return;
			}

			if (extTrunk.getExtensionParameter().getOptionsResponseCode() == 911) {
				return;
			}

			extTrunk.setAlive(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
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
