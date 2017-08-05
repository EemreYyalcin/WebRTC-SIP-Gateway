package sipserver.com.service;

import java.util.Properties;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;

public class ServiceProvider {

	private RegisterService resgisterService = new RegisterService(this, 10);
	
	private static StackLogger logger = CommonLogger.getLogger(ServiceProvider.class);

	private Properties tempList = new Properties();

	public ServiceProvider() {

		tempList.setProperty("1001", "test1001");
		tempList.setProperty("1002", "test1002");
		tempList.setProperty("1003", "test1003");
		tempList.setProperty("1004", "test1004");

	}

	public int isAuthExtension(Extension extension, DigestServerAuthenticationHelper dsam, Request request) throws Exception {
		Extension ext = (Extension) getResgisterService().getRegisterControlTask().isRegistered(extension.getExten());
		if (ext != null) {
			if (ext.getHost().equals(extension.getHost())) {
				getResgisterService().getRegisterControlTask().registerTask(extension.getExpiresTime(), extension.getExten(), extension);
				return Response.OK;
			}
			getResgisterService().getRegisterControlTask().unRegisterTask(extension.getExten());
			return Response.UNAUTHORIZED;
		}

		// TODO: Authenticate with database
		String pass = tempList.getProperty(extension.getExten());
		if (pass == null) {
			logger.logFatalError("Forbidden 1");
			return Response.FORBIDDEN;
		}

		if (!isHaveAuthenticateHeader(request)) {
			return Response.UNAUTHORIZED;
		}

		if (!dsam.doAuthenticatePlainTextPassword(request, pass)) {
			logger.logFatalError("Forbidden 2");
			return Response.FORBIDDEN;
		}

		getResgisterService().getRegisterControlTask().registerTask(extension.getExpiresTime(), extension.getExten(), extension);
		return Response.OK;
	}

	private boolean isHaveAuthenticateHeader(Request request) {
		if (request.getHeader(WWWAuthenticate.NAME) != null) {
			return true;
		}
		if (request.getHeader(PAssertedIdentityHeader.NAME) != null) {
			return true;
		}
		if (request.getHeader(ProxyAuthenticateHeader.NAME) != null) {
			return true;
		}
		if (request.getHeader(ProxyAuthorizationHeader.NAME) != null) {
			return true;
		}
		return false;
	}

	public RegisterService getResgisterService() {
		return resgisterService;
	}

	public void setResgisterService(RegisterService resgisterService) {
		this.resgisterService = resgisterService;
	}

}
