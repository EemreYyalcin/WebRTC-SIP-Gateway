package sipserver.com.executer.task;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Task extends Thread {

	private int intervalSeconds = 10;
	private boolean running = true;
	private Consumer<String> operation;
	private String taskName;

	@Override
	public void run() {
		try {
			while (running) {
				CompletableFuture.runAsync(() -> {
					operation.accept(taskName);
				});
				Thread.sleep(intervalSeconds);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopTask() {
		running = false;
	}

	public void setInterval(int intervalSeconds) {
		this.intervalSeconds = intervalSeconds;
	}

	public void setOperation(Consumer<String> operation) {
		this.operation = operation;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

}
