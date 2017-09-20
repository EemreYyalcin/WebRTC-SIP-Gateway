package sipserver.com.service.register;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.header.ContactHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.util.log.LogTest;

public class RegisterServiceIn extends Service {

	private static StackLogger logger = CommonLogger.getLogger(RegisterServiceIn.class);

	public RegisterServiceIn() {
		super(logger);
		ServerCore.getServerCore().addLocalExtension(new Extension("1001", "test1001"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1002", "test1002"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1003", "test1003"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1004", "test1004"));
		ServerCore.getServerCore().addLocalExtension(new Extension("1005", "test1005"));

//		ServerCore.getServerCore().addLocalExtension(new Extension("9001", "test9001"));
//		ServerCore.getServerCore().addLocalExtension(new Extension("9002", "test9002"));

	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport) throws Exception {
		String message = requestEvent.getRequest().toString();
		LogTest.log("XXDEBUG 0 " + requestEvent.getRequest().getRequestURI());
		ServerTransaction serverTransaction = null;
		try {
			serverTransaction = transport.getSipProvider().getNewServerTransaction(requestEvent.getRequest());
		} catch (TransactionAlreadyExistsException exception) {
			LogTest.log("Transaction Already Exist " + requestEvent.getRequest().getRequestURI());
		}

		try {
			// logger.logFatalError("RegisterRequestProcess:\r\n" + message);
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
				code = registerExtension(extIncoming.getExten(), extIncoming.getHost(), requestEvent);
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

	public int registerExtension(String exten, String host, RequestEvent requestEvent) throws Exception {

		Extension extLocal = ServerCore.getServerCore().getLocalExtension(exten);
		if (extLocal == null) {
			extLocal = ServerCore.getServerCore().getTrunkExtension(exten);
			if (extLocal == null) {
				return Response.FORBIDDEN;
			}
		}

		if (extLocal.isRegister()) {
			if (extLocal.getHost().equals(host)) {
				updateRegister(host, extLocal);
				return Response.OK;
			}
			unRegisterLocalExtension(exten);
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
		updateRegister(host, extLocal);
		return Response.OK;
	}

	private void updateRegister(String host, Extension extLocal) {
		extLocal.setHost(host);
		extLocal.keepRegistered();
	}

	public void unRegisterLocalExtension(String exten) {
		Extension extension = ServerCore.getServerCore().getLocalExtension(exten);
		if (extension == null) {
			return;
		}
		extension.setRegister(false);
	}

}
