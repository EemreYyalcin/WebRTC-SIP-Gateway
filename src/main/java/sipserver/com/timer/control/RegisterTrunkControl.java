package sipserver.com.timer.control;

import sipserver.com.domain.Extension;
import sipserver.com.executer.Transaction;
import sipserver.com.service.RegisterTrunkService;
import sipserver.com.timer.Task;

public class RegisterTrunkControl extends Task {

	private RegisterTrunkService registerTrunkService;

	public RegisterTrunkControl(int timeout, String taskId, RegisterTrunkService registerTrunkService) {
		super(timeout, taskId);
		setRegisterTrunkService(registerTrunkService);
	}

	@Override
	public void endTask() {
		getRegisterTrunkService().registerTrunk((Extension) getRegisterTrunkService().getTrunkExtensionList().get(getTaskId()));
	}

	@Override
	public boolean isRegistered() {
		return false;
	}

	public RegisterTrunkService getRegisterTrunkService() {
		return registerTrunkService;
	}

	public void setRegisterTrunkService(RegisterTrunkService registerTrunkService) {
		this.registerTrunkService = registerTrunkService;
	}

}
