package sipserver.com.service.register;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.ContactHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;

public class RegisterServiceIn extends Service {

	private static StackLogger logger = CommonLogger.getLogger(RegisterServiceIn.class);

	public RegisterServiceIn() {
		super(logger);
		ServerCore.getServerCore().addLocalExtension(new Extension("1001", "test1001", "192.168.1.100"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1002", "test1002", "192.168.1.100"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1003", "test1003", "192.168.1.100"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1004", "test1004", "192.168.1.100"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1005", "test1005", "192.168.1.100"));
	}

	@Override
	public void processRequest(RequestEvent requestEvent) throws Exception {
		String message = requestEvent.getRequest().toString();
		SipServerTransport transport = ServerCore.getTransport(requestEvent.getRequest());
		if (transport == null) {
			logger.logFatalError("Transport is null\r\n");
			throw new Exception();
		}

		ServerTransaction serverTransaction = transport.getSipProvider().getNewServerTransaction(requestEvent.getRequest());
		try {
			logger.logFatalError("RegisterRequestProcess:\r\n" + message);
			sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.TRYING);
			int code = 200;
			try {
				ContactHeader contactHeader = (ContactHeader) requestEvent.getRequest().getHeader(ContactHeader.NAME);
				if (contactHeader == null) {
					sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.UNAUTHORIZED);
					logger.logFatalError("Contact Header is Null. Message:" + message);
					return;
				}

				Extension extIncoming = new Extension(contactHeader);
				if (extIncoming == null || extIncoming.getExten() == null || extIncoming.getHost() == null) {
					sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_REQUEST);
					return;
				}

				Extension extLocal = ServerCore.getServerCore().getLocalExtension(extIncoming.getExten());
				if (extLocal == null) {
					sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.FORBIDDEN);
					return;
				}

				code = registerExtension(extIncoming, extLocal, requestEvent);
				if (code == Response.UNAUTHORIZED) {
					Response challengeResponse = transport.getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, requestEvent.getRequest());
					transport.getDigestServerAuthentication().generateChallenge(transport.getHeaderFactory(), challengeResponse, "nist.gov");
					serverTransaction.sendResponse(challengeResponse);
				}
			} catch (Exception e) {
				sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.UNAUTHORIZED);
				e.printStackTrace();
				logger.logFatalError("Message Error. Message:" + message);
				return;
			}
			sendResponseMessage(serverTransaction, requestEvent.getRequest(), code);
		} catch (Exception e) {
			e.printStackTrace();
			logger.logFatalError("Message Error. Message:" + message);
			sendResponseMessage(serverTransaction, requestEvent.getRequest(), Response.BAD_EVENT);
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent) {
		// NON
	}

	protected void sendResponseMessage(ServerTransaction serverTransaction, Request request, int responseCode) {
		try {
			if (serverTransaction == null || request == null) {
				throw new Exception();
			}
			SipServerTransport sipServerTransport = ServerCore.getTransport(request);
			if (sipServerTransport == null) {
				getLogger().logFatalError("Transport is Null");
				throw new Exception();
			}
			Response response = sipServerTransport.getMessageFactory().createResponse(responseCode, request);
			if (response != null) {
				serverTransaction.sendResponse(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int registerExtension(Extension extIncoming, Extension extLocal, RequestEvent requestEvent) throws Exception {
		if (ServerCore.getServerCore().checkRegisterationExtension(extIncoming.getExten())) {
			if (extLocal.getHost().equals(extIncoming.getHost())) {
				updateRegister(extLocal);
				return Response.OK;
			}
			unRegisterLocalExtension(extIncoming.getExten());
			return Response.UNAUTHORIZED;
		}
		if (!isHaveAuthenticateHeader(requestEvent)) {
			return Response.UNAUTHORIZED;
		}

		if (extLocal.getPass() == null) {
			return Response.FORBIDDEN;
		}

		if (!ServerCore.getTransport(requestEvent.getRequest()).getDigestServerAuthentication().doAuthenticatePlainTextPassword(requestEvent.getRequest(), extLocal.getPass())) {
			logger.logFatalError("Forbidden 2");
			return Response.FORBIDDEN;
		}
		updateRegister(extLocal);
		return Response.OK;
	}

	private void updateRegister(Extension extLocal) {
		extLocal.setRegister(true);
		beginTask(extLocal.getExten(), extLocal.getExpiresTime(), extLocal.getExten());
	}

	public void unRegisterLocalExtension(String exten) {
		Extension extension = ServerCore.getServerCore().getLocalExtension(exten);
		if (extension == null) {
			return;
		}
		extension.setRegister(false);
	}

	@Override
	public void beginTask(String taskId, int timeout, String exten) {
		ServerCore.getServerCore().getTimerService().registerTask(taskId + RegisterServiceIn.class.getName(), timeout);
		putTransaction(taskId, exten);
	}

	@Override
	public void endTask(String taskId) {
		String exten = getTransaction(taskId);
		if (exten == null) {
			return;
		}
		Extension extension = ServerCore.getServerCore().getLocalExtension(exten);
		if (extension == null) {
			return;
		}
		extension.setRegister(false);
	}

}
