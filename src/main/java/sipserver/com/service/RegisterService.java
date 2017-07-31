package sipserver.com.service;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;

public class RegisterService {

	private ServiceProvider serviceProvider;

	public RegisterService(ServiceProvider serviceProvicder) {
		setServiceProvider(serviceProvicder);
	}

	public int register(Extension extension, DigestServerAuthenticationHelper dsam, Request request) throws Exception {
		if (extension == null || extension.getExten() == null || extension.getHost() == null) {
			return Response.BAD_REQUEST;
		}
		return serviceProvider.getExtension(extension, dsam, request);
	}

	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

}
