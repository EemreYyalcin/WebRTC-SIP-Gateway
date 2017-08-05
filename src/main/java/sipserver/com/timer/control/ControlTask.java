package sipserver.com.timer.control;

import java.util.Properties;

import sipserver.com.timer.SipServerTimer;

public abstract class ControlTask {

	private Properties list = new Properties();
	private SipServerTimer sipServerTimer;

	public ControlTask(int interval) {
		setSipServerTimer(new SipServerTimer(interval, this));
		getSipServerTimer().start();
	}

	public abstract void endTask(String taskId);

	public abstract void registerTask(int timeout, String taskId, Object exten);
	
	public abstract Object isRegistered(String taskId);
	
	public abstract void unRegisterTask(String taskId);

	public Properties getList() {
		return list;
	}

	public void setList(Properties list) {
		this.list = list;
	}

	public SipServerTimer getSipServerTimer() {
		return sipServerTimer;
	}

	public void setSipServerTimer(SipServerTimer sipServerTimer) {
		this.sipServerTimer = sipServerTimer;
	}

}
