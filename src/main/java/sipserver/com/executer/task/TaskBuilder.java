package sipserver.com.executer.task;

import java.util.function.Consumer;

import sipserver.com.executer.core.ServerCore;

public class TaskBuilder {

	public static Task createAndStartTask(int intervalSeconds, Consumer<String> operation, String taskName) {
		Task task = new Task();
		task.setOperation(operation);
		task.setInterval(intervalSeconds);
		task.setTaskName(taskName);
		task.start();
		ServerCore.getCoreElement().addTask(task);
		return task;
	}

}
