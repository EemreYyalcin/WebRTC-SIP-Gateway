package sipserver.com.timer;

public abstract class Task {

	private long time;
	private String taskId;

	public Task(int timeout, String taskId) {
		setTime(System.currentTimeMillis() + timeout * 1000);
		setTaskId(taskId);
	}
	
	public abstract void endTask();

	public abstract boolean isRegistered();

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	
}
