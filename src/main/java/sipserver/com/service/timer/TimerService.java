package sipserver.com.service.timer;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.service.register.RegisterServiceIn;
import sipserver.com.service.register.RegisterServiceOut;

public class TimerService extends Thread {

	private Properties currentTaskList = new Properties();
	private ArrayList<String> removedList = new ArrayList<String>();

	private static StackLogger logger = CommonLogger.getLogger(TimerService.class);

	public TimerService() {
		start();
	}

	@Override
	public void run() {
		Timer timer = new Timer();
		TimerTask schedule = new TimerTask() {
			@Override
			public void run() {
				processTaskList();
			}
		};
		timer.schedule(schedule, 0, 2);
	}

	private void processTaskList() {
		synchronized (getCurrentTaskList()) {
			Set<Object> keys = getCurrentTaskList().keySet();
			if (keys == null || keys.size() == 0) {
				return;
			}
			for (Object key : keys) {
				long time = (Long) getCurrentTaskList().get(key);
				if (System.currentTimeMillis() > time) {
					endTask((String) key);
					getRemovedList().add((String) key);
				}
			}
			for (String taskId : getRemovedList()) {
				getCurrentTaskList().remove(taskId);
			}
			getRemovedList().clear();
		}

	}

	private void endTask(String key) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (key.indexOf(RegisterServiceIn.class.getName()) > 0) {
					ServerCore.getServerCore().getRegisterServiceIn().endTask(key);
					return;
				}
				if (key.indexOf(RegisterServiceOut.class.getName()) > 0) {
					ServerCore.getServerCore().getRegisterServiceOut().endTask(key);
					return;
				}

			}
		}).start();
	}

	public void registerTask(String taskId, int timeout) {
		synchronized (getCurrentTaskList()) {
			getCurrentTaskList().put(taskId, System.currentTimeMillis() + (timeout * 1000));
		}
	}

	private Properties getCurrentTaskList() {
		return currentTaskList;
	}

	public ArrayList<String> getRemovedList() {
		return removedList;
	}

}
