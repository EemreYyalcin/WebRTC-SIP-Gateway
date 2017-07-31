package sipserver.com.service;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;

public class ServiceProvider {

	private Properties extensionList = new Properties();

	private RegisterService resgisterService = new RegisterService(this);

	public int getExtension(Extension extension, DigestServerAuthenticationHelper dsam, Request request) throws Exception {
		Extension ext = (Extension) extensionList.get((extension.getExten() + extension.getHost()).hashCode());
		if (ext != null) {
			if (ext.getHost().equals(extension.getHost())) {
				return Response.OK;
			}
			extensionList.remove(extension.getExten());
			return Response.UNAUTHORIZED;
		}

		// boolean result = getAuthenticate for Database with dsam and pass
		// if(!result){
		// throw new Exception();
		// }

		if (!isHaveAuthenticateHeader(request)) {
			return Response.UNAUTHORIZED;
		}

		if (!dsam.doAuthenticatePlainTextPassword(request, "test1001")) {
			return Response.FORBIDDEN;
		}

		extension.setTimeStamp(System.currentTimeMillis());
		getExtensionList().put((extension.getExten() + extension.getHost()).hashCode(), extension);
		return Response.OK;
	}

	private int getDifferenceTime(long afterTime) {
		long now = System.currentTimeMillis();
		return (int) TimeUnit.MICROSECONDS.toSeconds(now - afterTime);
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

	public Properties getExtensionList() {
		return extensionList;
	}

	public void setExtensionList(Properties extensionList) {
		this.extensionList = extensionList;
	}

	public RegisterService getResgisterService() {
		return resgisterService;
	}

	public void setResgisterService(RegisterService resgisterService) {
		this.resgisterService = resgisterService;
	}

}
