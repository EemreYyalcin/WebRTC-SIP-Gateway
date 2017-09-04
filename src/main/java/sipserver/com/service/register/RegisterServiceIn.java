package sipserver.com.service.register;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.util.Properties;

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

	private Properties transaction = new Properties();

	public RegisterServiceIn() {
		super(logger);
		ServerCore.getServerCore().addLocalExtension(new Extension("1001", "test1001"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1002", "test1002"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1003", "test1003"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1004", "test1004"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1005", "test1005"));
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport) throws Exception {
		String message = requestEvent.getRequest().toString();
		ServerTransaction serverTransaction = transport.getSipProvider().getNewServerTransaction(requestEvent.getRequest());
		try {
			logger.logFatalError("RegisterRequestProcess:\r\n" + message);
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.TRYING));
			int code = 200;
			try {
				ContactHeader contactHeader = (ContactHeader) requestEvent.getRequest().getHeader(ContactHeader.NAME);
				if (contactHeader == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.UNAUTHORIZED));
					logger.logFatalError("Contact Header is Null. Message:" + message);
					return;
				}

				Extension extIncoming = new Extension(contactHeader);
				extIncoming.setTransportType(transport);
				if (extIncoming == null || extIncoming.getExten() == null || extIncoming.getHost() == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.BAD_REQUEST));
					return;
				}

				Extension extLocal = ServerCore.getServerCore().getLocalExtension(extIncoming.getExten());
				if (extLocal == null) {
					extLocal = ServerCore.getServerCore().getTrunkExtension(extIncoming.getExten());
					if (extLocal == null) {
						ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.FORBIDDEN));
						return;
					}
				}

				code = registerExtension(extIncoming, extLocal, requestEvent);
				if (code == Response.UNAUTHORIZED) {
					Response challengeResponse = transport.getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, requestEvent.getRequest());
					transport.getDigestServerAuthentication().generateChallenge(transport.getHeaderFactory(), challengeResponse, "nist.gov");
					serverTransaction.sendResponse(challengeResponse);
				}
			} catch (Exception e) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.UNAUTHORIZED));
				logger.logFatalError("Message Error. Message:" + message);
				e.printStackTrace();
				return;
			}
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), code));
		} catch (Exception e) {
			e.printStackTrace();
			logger.logFatalError("Message Error. Message:" + message);
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction, createResponseMessage(requestEvent.getRequest(), Response.BAD_EVENT));
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		// NON
	}

	protected Response createResponseMessage(Request request, int responseCode) {
		try {
			if (request == null) {
				throw new Exception();
			}
			SipServerTransport sipServerTransport = ServerCore.getTransport(request);
			if (sipServerTransport == null) {
				getLogger().logFatalError("Transport is Null");
				throw new Exception();
			}
			Response response = sipServerTransport.getMessageFactory().createResponse(responseCode, request);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private int registerExtension(Extension extIncoming, Extension extLocal, RequestEvent requestEvent) throws Exception {
		if (extLocal.isRegister()) {
			if (extLocal.getHost().equals(extIncoming.getHost())) {
				updateRegister(extIncoming, extLocal);
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
		updateRegister(extIncoming, extLocal);
		return Response.OK;
	}

	private void updateRegister(Extension extIncoming, Extension extLocal) {
		extLocal.setRegister(true);
		extLocal.setHost(extIncoming.getHost());
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
	public void beginTask(String taskId, int timeout, Object exten) {
		ServerCore.getServerCore().getTimerService().registerTask(taskId + RegisterServiceIn.class.getName(), timeout);
		putTransaction(taskId, (String) exten);
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
		logger.logFatalError("UnRegister ExtensionLocal:" + exten);
		extension.setRegister(false);
	}

	public String getTransaction(String id) {
		String value = transaction.getProperty(id);
		transaction.remove(id);
		return value;
	}

	public void putTransaction(String key, String value) {
		transaction.setProperty(key, value);
	}

}
