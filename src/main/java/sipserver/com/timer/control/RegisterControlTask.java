package sipserver.com.timer.control;

import sipserver.com.timer.Task;

public class RegisterControlTask extends ControlTask {

	public RegisterControlTask(int interval) {
		super(interval);
	}

	@Override
	public void endTask(String taskId) {
		getList().remove(taskId);
	}

	@Override
	public void registerTask(int timeout, String taskId, Object exten) {
		getList().put(taskId, exten);
		getSipServerTimer().registerTask(new Task(timeout, taskId));
	}

	@Override
	public Object isRegistered(String taskId) {
		return getList().get(taskId);
	}

	@Override
	public void unRegisterTask(String taskId) {
		getList().remove(taskId);
	}

}
