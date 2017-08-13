package sipserver.com.service;

import java.util.Properties;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.message.Request;
import sipserver.com.domain.Extension;
import sipserver.com.executer.RegisterTransactionOut;
import sipserver.com.executer.Transaction;
import sipserver.com.server.SipServer;
import sipserver.com.service.util.GeneraterService;
import sipserver.com.timer.control.RegisterControl;

public class RegisterTrunkService {

	private SipServer sipServer;
	private Properties trunkExtensionList = new Properties();
	private Properties registerExtensionTrunkList = new Properties();

	private static StackLogger logger = CommonLogger.getLogger(RegisterTrunkService.class);

	public RegisterTrunkService(SipServer sipServer) {
		setSipServer(sipServer);
		registerTrunk(new Extension("1001", "test1001", "192.168.1.108", 5060));
	}

	public boolean registerTrunk(Extension extension) {
		if (extension == null) {
			logger.logFatalError("Extension null");
			return false;
		}

		String callId = GeneraterService.getUUid(10);
		Transaction transaction = getSipServer().getTransactionManager().addTransactionOut(callId, Request.REGISTER, extension);
		RegisterTransactionOut registerTransactionOut = (RegisterTransactionOut) transaction;
		registerTransactionOut.processRequestTransaction(null);
		return false;
	}

	public void unRegisterExtension(Extension extension) {
		getRegisterExtensionTrunkList().remove(extension.getExten());
	}

	public boolean isRegisterExtension(Extension extension) {
		RegisterControl registerControl = (RegisterControl) getRegisterExtensionTrunkList().get(extension.getExten());
		if (registerControl == null) {
			return false;
		}
		return registerControl.isRegistered();
	}

	public Properties getTrunkExtensionList() {
		return trunkExtensionList;
	}

	public SipServer getSipServer() {
		return sipServer;
	}

	public void setSipServer(SipServer sipServer) {
		this.sipServer = sipServer;
	}

	public Properties getRegisterExtensionTrunkList() {
		return registerExtensionTrunkList;
	}

}
