package sipserver.com.service;

import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;

import java.util.EventObject;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.message.Message;

import sipserver.com.server.SipServerTransport;

public abstract class Service {

	private StackLogger logger;

	public Service(StackLogger logger) {
		setLogger(logger);
	}

	public abstract void processRequest(RequestEvent requestEvent, SipServerTransport transport) throws Exception;

	public abstract void processResponse(ResponseEvent responseEvent, SipServerTransport transport);

	public abstract void beginTask(String taskId, int timeout, Object object);

	public abstract void endTask(String taskId);

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

}
