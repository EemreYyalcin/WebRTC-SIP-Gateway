package sipserver.com.timer;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.timer.control.ControlTask;

public class SipServerTimer extends Thread {

	private int interval = 2;
	private boolean stopTimer = false;
	private Timer timer;
	private Properties currentTaskList = new Properties();
	private ArrayList<Task> addedTaskList = new ArrayList<Task>();
	private ControlTask controlTask;

	private static StackLogger logger = CommonLogger.getLogger(SipServerTimer.class);

	public SipServerTimer(int interval, ControlTask controlTask) {
		setInterval(interval);
		setControlTask(controlTask);
		timer = new Timer();
	}

	@Override
	public void run() {
		TimerTask schedule = new TimerTask() {
			@Override
			public void run() {
				while (!stopTimer) {
					processTaskList();
				}
			}

		};
		timer.schedule(schedule, 0, getInterval());
	}

	private void processTaskList() {
		Set<Object> keys = currentTaskList.keySet();
		if (keys != null) {
			for (Object key : keys) {
				Task task = (Task) currentTaskList.get(key);
				if (System.currentTimeMillis() > task.getTime()) {
					logger.logFatalError("UnRegister key:" + key);
					getControlTask().endTask((String) key);
					currentTaskList.remove(key);
				}
			}
		}
		while (addedTaskList.size() > 0) {
			logger.logFatalError("Register TaskId:" + addedTaskList.get(0).getTaskId() + ",timeout:" + (addedTaskList.get(0).getTime() - System.currentTimeMillis()));
			currentTaskList.put(addedTaskList.get(0).getTaskId(), addedTaskList.get(0));
			addedTaskList.remove(0);
		}

	}

	public void registerTask(Task task) {
		addedTaskList.add(task);
	}
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void stopTimer() {
		stopTimer = true;
		timer.cancel();
	}

	public ControlTask getControlTask() {
		return controlTask;
	}

	public void setControlTask(ControlTask controlTask) {
		this.controlTask = controlTask;
	}

}
