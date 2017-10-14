package sipserver.com.service.register;

import java.util.Objects;

import javax.sip.ClientTransaction;
import javax.sip.DialogTerminatedEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.ContactHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.server.auth.AccountManagerImpl;
import sipserver.com.service.Service;
import sipserver.com.service.util.AliasService;
import sipserver.com.service.util.ExceptionService;
import sipserver.com.service.util.message.CreateMessageService;

public class RegisterServiceOut extends Service {

	private static StackLogger logger = CommonLogger.getLogger(RegisterServiceOut.class);

	public RegisterServiceOut() {
		super(logger);
		// ServerCore.getCoreElement().addTrunkExtension(new Extension("9001",
		// "test9001", "192.168.1.105"));
		// ServerCore.getCoreElement().addTrunkExtension(new Extension("9002",
		// "test9002", "192.168.1.105"));
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport, ServerTransaction serverTransaction) throws Exception {
		// NON
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		try {
			Objects.requireNonNull(responseEvent.getClientTransaction());
			Objects.requireNonNull(responseEvent.getClientTransaction().getRequest());

			ContactHeader contactHeader = (ContactHeader) responseEvent.getClientTransaction().getRequest().getHeader(ContactHeader.NAME);
			ExceptionService.checkNullObject(contactHeader);
			Extension trunkExtensionIncoming = CreateMessageService.createExtension(contactHeader);
			ExceptionService.checkNullObject(trunkExtensionIncoming);
			int statusCode = responseEvent.getResponse().getStatusCode();
			Extension trunkExtensionLocal = ServerCore.getCoreElement().getTrunkExtension(trunkExtensionIncoming.getExten());
			ExceptionService.checkNullObject(trunkExtensionLocal);

			trunkExtensionLocal.getExtensionParameter().setRegisterResponseRecieved(true);
			trunkExtensionLocal.getExtensionParameter().setRegisterResponseCode(statusCode);

			String branch = AliasService.getBranch(responseEvent.getResponse());
			ExceptionService.checkNullObject(branch);

			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				if (!isHaveAuthenticateHeader(responseEvent)) {
					logger.logFatalError("Transaction is dead ");
					throw new Exception();
				}
				AuthenticationHelper authenticationHelper = ((SipStackExt) transport.getSipStack()).getAuthenticationHelper(new AccountManagerImpl(ServerCore.getCoreElement().getTrunkExtension(trunkExtensionLocal.getExten())), transport.getHeaderFactory());
				ClientTransaction clientTransaction = authenticationHelper.handleChallenge(responseEvent.getResponse(), responseEvent.getClientTransaction(), transport.getSipProvider(), 5, false);
				ServerCore.getServerCore().getTransportService().sendRequestMessage(clientTransaction);
				return;
			}
			if (statusCode == Response.FORBIDDEN) {
				logger.logFatalError("Forbidden " + trunkExtensionLocal.getExten());
				logger.logFatalError("Transaction is dead " + trunkExtensionLocal.getExten());
				return;
			}
			if (statusCode == Response.OK) {
				logger.logFatalError("Registered Trunk " + trunkExtensionLocal.getExten());
			}
			trunkExtensionLocal.setRegister(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void register(Extension extTrunk) {
		try {
			Request requestMessage = CreateMessageService.createRegisterMessage(extTrunk);
			SipServerTransport transport = ServerCore.getTransport(requestMessage);
			Objects.requireNonNull(transport);
			ServerCore.getServerCore().getTransportService().sendRequestMessage(transport.getSipProvider().getNewClientTransaction(requestMessage));
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
