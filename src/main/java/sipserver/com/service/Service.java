package sipserver.com.service;

import java.util.EventObject;
import java.util.Properties;

import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.message.Message;

public abstract class Service {

	private StackLogger logger;
	private Properties transaction = new Properties();

	public Service(StackLogger logger) {
		setLogger(logger);
	}

	public abstract void processRequest(RequestEvent requestEvent) throws Exception;
	public abstract void processResponse(ResponseEvent responseEvent);
	public abstract void beginTask(String taskId, int timeout, String exten);
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

	public String getTransaction(String id) {
		String value = transaction.getProperty(id);
		transaction.remove(id);
		return value;
	}
	public void putTransaction(String key, String value){
		transaction.setProperty(key, value);
	}

}
