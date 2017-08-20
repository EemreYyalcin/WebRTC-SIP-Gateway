package sipserver.com.service;

import java.util.EventObject;
import java.util.Properties;

import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;

public abstract class Service {

	private StackLogger logger;
	private static String NAME;
	private Properties generalTransaction = new Properties();
	private Properties callIdFromTask = new Properties();
	private Properties extenFromCallIdForAcknowledge = new Properties();

	public Service(StackLogger logger, String name) {
		setLogger(logger);
		setNAME(name);
	}

	public abstract void processRequest(RequestEvent requestEvent) throws Exception;
	public abstract void processResponse(ResponseEvent responseEvent);
	public abstract void beginTask(String taskId, int timeout, String exten);
	public abstract void endTask(String taskId);

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

	public boolean isHaveAuthenticateHeader(EventObject event) {
		Message message = null;
		if (event instanceof RequestEvent) {
			message = ((RequestEvent) event).getRequest();
		} else {
			message = ((ResponseEvent) event).getResponse();
		}

		if (message.getHeader(WWWAuthenticate.NAME) != null) {
			return true;
		}
		if (message.getHeader(PAssertedIdentityHeader.NAME) != null) {
			return true;
		}
		if (message.getHeader(ProxyAuthenticateHeader.NAME) != null) {
			return true;
		}
		if (message.getHeader(ProxyAuthorizationHeader.NAME) != null) {
			return true;
		}
		return false;
	}

	public StackLogger getLogger() {
		return logger;
	}

	public void setLogger(StackLogger logger) {
		this.logger = logger;
	}

	public static String getNAME() {
		return NAME;
	}

	public static void setNAME(String nAME) {
		NAME = nAME;
	}

	public abstract Request createRegisterMessage(Extension extension);

	public Properties getGeneralTransaction() {
		return generalTransaction;
	}

	public Properties getCallIdFromTask() {
		return callIdFromTask;
	}

	public Properties getExtenFromCallIdForAcknowledge() {
		return extenFromCallIdForAcknowledge;
	}

}
