package sipserver.com.service.register;

import javax.sip.DialogTerminatedEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.service.util.ExceptionService;
import sipserver.com.service.util.message.CreateMessageService;

public class RegisterServiceIn extends Service {

	private static StackLogger logger = CommonLogger.getLogger(RegisterServiceIn.class);

	public RegisterServiceIn() {
		super(logger);
		ServerCore.getServerCore().addLocalExtension(new Extension("1001", "test1001"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1002", "test1002"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1003", "test1003"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1004", "test1004"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1005", "test1005"));

		// ServerCore.getServerCore().addLocalExtension(new Extension("9001",
		// "test9001"));
		// ServerCore.getServerCore().addLocalExtension(new Extension("9002",
		// "test9002"));

	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport, ServerTransaction serverTransaction) throws Exception {
		try {
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.TRYING, null);
			try {
				CallIdHeader callIDHeader = (CallIdHeader) requestEvent.getRequest().getHeader(CallIdHeader.NAME);
				ExceptionService.checkNullObject(callIDHeader);

				ContactHeader contactHeader = (ContactHeader) requestEvent.getRequest().getHeader(ContactHeader.NAME);
				if (contactHeader == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.UNAUTHORIZED, null);
					logger.logFatalError("Contact Header is Null. Message:");
					return;
				}

				Extension extIncoming = CreateMessageService.createExtension(contactHeader);
				if (extIncoming == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_REQUEST, null);
					return;
				}
				extIncoming.setTransportType(transport);
				if (extIncoming == null || extIncoming.getExten() == null || extIncoming.getHost() == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_REQUEST, null);
					return;
				}

				Extension extLocal = ServerCore.getServerCore().getLocalExtension(extIncoming.getExten());
				if (extLocal == null) {
					extLocal = ServerCore.getServerCore().getTrunkExtension(extIncoming.getExten());
					if (extLocal == null) {
						ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.FORBIDDEN, null);
						return;
					}
				}

				ViaHeader viaHeader = (ViaHeader) requestEvent.getRequest().getHeader(ViaHeader.NAME);
				if (viaHeader == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_REQUEST, null);
					return;
				}
				extLocal.setPort(extIncoming.getPort());
				extLocal.setHost(extIncoming.getHost());
				if (extLocal.isRegister()) {
					if (!extLocal.getHost().equals(extIncoming.getHost())) {
						unRegisterLocalExtension(extLocal);
						ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.UNAUTHORIZED, null);
						return;
					}
					extLocal.keepRegistered();
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.OK, null);
					return;
				}
				if (!isHaveAuthenticateHeader(requestEvent)) {
					Response challengeResponse = transport.getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, requestEvent.getRequest());
					transport.getDigestServerAuthentication().generateChallenge(transport.getHeaderFactory(), challengeResponse, "nist.gov");
					ProxyAuthenticateHeader proxyAuthenticateHeader = (ProxyAuthenticateHeader) challengeResponse.getHeader(ProxyAuthenticateHeader.NAME);
					if (proxyAuthenticateHeader == null) {
						throw new Exception();
					}
					proxyAuthenticateHeader.setParameter("username", extIncoming.getExten());
					serverTransaction.sendResponse(challengeResponse);
					return;
				}

				if (extLocal.getPass() == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.FORBIDDEN, null);
					return;
				}

				if (!ServerCore.getTransport(requestEvent.getRequest()).getDigestServerAuthentication().doAuthenticatePlainTextPassword(requestEvent.getRequest(), extLocal.getPass())) {
					logger.logFatalError("Forbidden 2");
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.FORBIDDEN, null);
					return;
				}
				extLocal.keepRegistered();
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.OK, null);

			} catch (Exception e) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.UNAUTHORIZED, null);
				logger.logFatalError("Message Error. Message:");
				e.printStackTrace();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.logFatalError("Message Error. Message:");
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT, null);
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		// NON
	}

	public void unRegisterLocalExtension(Extension extension) {
		extension.setRegister(false);
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent event) {
		// LogTest.log(this, "Dialog terminated Register " +
		// event.getDialog().getCallId());
	}

	@Override
	public void processTimeout(TimeoutEvent timeoutEvent) {
		// LogTest.log(this, "Register Timeout request: " +
		// timeoutEvent.getServerTransaction().getRequest().toString());
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent terminatedEvent) {
		// LogTest.log(this, "Register Terminated request: " +
		// terminatedEvent.getServerTransaction().getRequest().toString());

	}

}
