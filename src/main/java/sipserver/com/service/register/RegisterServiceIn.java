package sipserver.com.service.register;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
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
import sipserver.com.service.util.CreateService;

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
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport) throws Exception {
		String message = requestEvent.getRequest().toString();
		ServerTransaction serverTransaction = getServerTransaction(transport.getSipProvider(), requestEvent.getRequest());
		if (serverTransaction == null) {
			return;
		}
		try {
			// logger.logFatalError("RegisterRequestProcess:\r\n" + message);
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction,requestEvent.getRequest(), Response.TRYING, null);
			int code = 200;
			try {
				CallIdHeader callIDHeader = (CallIdHeader) requestEvent.getRequest().getHeader(CallIdHeader.NAME);
				if (callIDHeader == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction,requestEvent.getRequest(), Response.BAD_EVENT, null);
					return;
				}

				ContactHeader contactHeader = (ContactHeader) requestEvent.getRequest().getHeader(ContactHeader.NAME);
				if (contactHeader == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction,requestEvent.getRequest(), Response.UNAUTHORIZED, null);
					logger.logFatalError("Contact Header is Null. Message:" + message);
					return;
				}

				Extension extIncoming = CreateService.createExtension(contactHeader);
				if (extIncoming == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction,requestEvent.getRequest(), Response.BAD_REQUEST, null);
					return;
				}
				extIncoming.setTransportType(transport);
				if (extIncoming == null || extIncoming.getExten() == null || extIncoming.getHost() == null) {
					ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction,requestEvent.getRequest(), Response.BAD_REQUEST, null);
					return;
				}
				code = registerExtension(extIncoming.getExten(), extIncoming.getHost(), requestEvent);
				if (code == Response.UNAUTHORIZED) {
					Response challengeResponse = transport.getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, requestEvent.getRequest());
					transport.getDigestServerAuthentication().generateChallenge(transport.getHeaderFactory(), challengeResponse, "nist.gov");
					ProxyAuthenticateHeader proxyAuthenticateHeader = (ProxyAuthenticateHeader) challengeResponse.getHeader(ProxyAuthenticateHeader.NAME);
					if (proxyAuthenticateHeader == null) {
						throw new Exception();
					}
					proxyAuthenticateHeader.setParameter("username", extIncoming.getExten());
					serverTransaction.sendResponse(challengeResponse);
				}
			} catch (Exception e) {
				ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction,requestEvent.getRequest(), Response.UNAUTHORIZED, null);
				logger.logFatalError("Message Error. Message:" + message);
				e.printStackTrace();
				return;
			}
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction,requestEvent.getRequest(), code, null);
		} catch (Exception e) {
			e.printStackTrace();
			logger.logFatalError("Message Error. Message:" + message);
			ServerCore.getServerCore().getTransportService().sendResponseMessage(serverTransaction,requestEvent.getRequest(), Response.BAD_EVENT, null);
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		// NON
	}

	public int registerExtension(String exten, String host, RequestEvent requestEvent) throws Exception {

		Extension extLocal = ServerCore.getServerCore().getLocalExtension(exten);
		if (extLocal == null) {
			extLocal = ServerCore.getServerCore().getTrunkExtension(exten);
			if (extLocal == null) {
				return Response.FORBIDDEN;
			}
		}

		ViaHeader viaHeader = (ViaHeader) requestEvent.getRequest().getHeader(ViaHeader.NAME);
		if (viaHeader == null) {
			return Response.BAD_EVENT;
		}
		int remotePort = viaHeader.getPort();

		if (extLocal.isRegister()) {
			if (extLocal.getHost().equals(host)) {
				updateRegister(host, remotePort, extLocal);
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
		updateRegister(host, remotePort, extLocal);
		return Response.OK;
	}

	private void updateRegister(String host, int port, Extension extLocal) {
		extLocal.setHost(host);
		extLocal.setPort(port);
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
