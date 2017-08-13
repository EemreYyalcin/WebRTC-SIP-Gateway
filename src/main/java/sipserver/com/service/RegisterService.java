package sipserver.com.service;

import java.util.Properties;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.domain.Extension;
import sipserver.com.server.SipServer;
import sipserver.com.service.util.SipMessageService;
import sipserver.com.timer.control.RegisterControl;

public class RegisterService {

	private Properties localExtensionList = new Properties();
	private Properties registerExtensionControlList = new Properties();
	private SipServer sipServer;

	private static StackLogger logger = CommonLogger.getLogger(RegisterService.class);

	public RegisterService(SipServer sipServer) {
		setSipServer(sipServer);
		getLocalExtensionList().put("1001", new Extension("1001", "test1001", "192.168.1.100"));
		getLocalExtensionList().put("1002", new Extension("1002", "test1002", "192.168.1.100"));
		getLocalExtensionList().put("1003", new Extension("1003", "test1003", "192.168.1.100"));
		getLocalExtensionList().put("1004", new Extension("1004", "test1004", "192.168.1.100"));
		getLocalExtensionList().put("1005", new Extension("1005", "test1005", "192.168.1.100"));
	}

	public int register(Extension extension, Request request) throws Exception {
		if (extension == null || extension.getExten() == null || extension.getHost() == null) {
			return Response.BAD_REQUEST;
		}
		return isAuthExtension(extension, request);
	}

	private int isAuthExtension(Extension extension, Request request) throws Exception {

		if (isRegisterExtension(extension)) {
			RegisterControl registerControl = (RegisterControl) getRegisterExtensionControlList().get(extension.getExten());
			if (registerControl.getExtension().getHost().equals(extension.getHost())) {
				int response = registerExtension(extension, request);
				if (response == Response.OK) {
					return Response.OK;
				}
				unRegisterExtension(extension);
				return response;
			}
			unRegisterExtension(extension);
			return Response.UNAUTHORIZED;
		}

		return registerExtension(extension, request);
	}

	public int registerExtension(Extension extension, Request request) {
		RegisterControl registerControl = (RegisterControl) registerExtensionControlList.get(extension.getExten());
		if (registerControl == null) {
			if (getLocalExtensionList().get(extension.getExten()) == null) {
				return Response.FORBIDDEN;
			}
			if (!SipMessageService.isHaveAuthenticateHeader(request)) {
				return Response.UNAUTHORIZED;
			}

			if (extension.getPass() == null) {
				return Response.FORBIDDEN;
			}

			if (!getSipServer().getDigestServerAuthentication().doAuthenticatePlainTextPassword(request, extension.getPass())) {
				logger.logFatalError("Forbidden 2");
				return Response.FORBIDDEN;
			}
			registerControl = new RegisterControl(extension);
			getSipServer().getSipServerTimer().registerTask(registerControl);
			getRegisterExtensionControlList().put(extension.getExten(), registerControl);
		}
		registerControl.getExtension().setOnline(true);
		registerControl.setTime(System.currentTimeMillis() + extension.getExpiresTime() * 1000);
		return Response.OK;
	}

	public void unRegisterExtension(Extension extension) {
		getRegisterExtensionControlList().remove(extension.getExten());
	}

	public boolean isRegisterExtension(Extension extension) {
		RegisterControl registerControl = (RegisterControl) getRegisterExtensionControlList().get(extension.getExten());
		if (registerControl == null) {
			return false;
		}
		return registerControl.isRegistered();
	}

	public Properties getLocalExtensionList() {
		return localExtensionList;
	}

	public Properties getRegisterExtensionControlList() {
		return registerExtensionControlList;
	}

	public SipServer getSipServer() {
		return sipServer;
	}

	public void setSipServer(SipServer sipServer) {
		this.sipServer = sipServer;
	}
}
