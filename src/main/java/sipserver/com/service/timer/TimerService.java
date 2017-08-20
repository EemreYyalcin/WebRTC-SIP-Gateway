package sipserver.com.service.timer;

import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.service.register.RegisterService;

public class TimerService extends Thread {

	private Properties currentTaskList = new Properties();

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
		timer.schedule(schedule, 0, 1);
	}

	private void processTaskList() {
		Set<Object> keys = getCurrentTaskList().keySet();
		if (keys != null) {
			for (Object key : keys) {
				long time = (Long) getCurrentTaskList().get(key);
				if (System.currentTimeMillis() > time) {
					logger.logFatalError("UnRegister key:" + key);
					endTask((String) key);
					getCurrentTaskList().remove(key);
				}
			}
		}
	}

	private void endTask(String key) {
		getCurrentTaskList().remove(key);
		if (key.indexOf(RegisterService.getNAME()) > 0) {
			ServerCore.getServerCore().getRegisterService().endTask(key);
		}
	}

	public void registerTask(String taskId, int timeout) {
		getCurrentTaskList().put(taskId, System.currentTimeMillis() + (timeout * 1000));
	}

	private Properties getCurrentTaskList() {
		return currentTaskList;
	}

}
