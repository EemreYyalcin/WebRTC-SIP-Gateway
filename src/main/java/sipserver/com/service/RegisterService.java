package sipserver.com.service;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;
import sipserver.com.timer.control.RegisterControlTask;

public class RegisterService {

	private ServiceProvider serviceProvider;
	private RegisterControlTask registerControlTask;

	public RegisterService(ServiceProvider serviceProvicder, int loopInterval) {
		setServiceProvider(serviceProvicder);
		setRegisterControlTask(new RegisterControlTask(loopInterval));
	}

	public int register(Extension extension, DigestServerAuthenticationHelper dsam, Request request) throws Exception {
		if (extension == null || extension.getExten() == null || extension.getHost() == null) {
			return Response.BAD_REQUEST;
		}
		return serviceProvider.isAuthExtension(extension, dsam, request);
	}

	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public RegisterControlTask getRegisterControlTask() {
		return registerControlTask;
	}

	public void setRegisterControlTask(RegisterControlTask registerControlTask) {
		this.registerControlTask = registerControlTask;
	}

}
