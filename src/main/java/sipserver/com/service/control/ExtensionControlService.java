package sipserver.com.service.control;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.executer.task.Task;
import sipserver.com.service.operational.OptionsService;
import sipserver.com.service.operational.RegisterService;
import sipserver.com.service.util.converter.Converter;

public class ExtensionControlService {

	public static void beginControl() {
		Consumer<String> trunkRegisterSendingOperationForUnregisterExten = (e) -> {
			if (Objects.nonNull(e) && e.length() > 0) {
				System.out.println(e);
			}
			processRegisterExtension(false);
		};

		Consumer<String> trunkRegisterSendingOperationForRegisterExten = (e) -> {
			if (Objects.nonNull(e) && e.length() > 0) {
				System.out.println(e);
			}
			processRegisterExtension(true);
		};

		Consumer<String> optionsSendingOperation = (e) -> {
			if (Objects.nonNull(e) && e.length() > 0) {
				System.out.println(e);
			}
			processOptionsExtension();
		};

		Task trunkRegisterForUnregisterTask = new Task(SipServerSharedProperties.registerSendingIntervallForUnRegisterExten, trunkRegisterSendingOperationForUnregisterExten, "trunkRegisterForUnregisterTask");
		Task trunkRegisterForRegisterTask = new Task(SipServerSharedProperties.registerSendingIntervallForRegisterExten, trunkRegisterSendingOperationForRegisterExten, "trunkRegisterForRegisterTask");
		Task optionsSendingOperationTask = new Task(SipServerSharedProperties.optionsSendingIntervallForRegisterExten, optionsSendingOperation, "optionsSendingOperationTask");
		ServerCore.getCoreElement().addTask(trunkRegisterForRegisterTask);
		ServerCore.getCoreElement().addTask(trunkRegisterForUnregisterTask);
		ServerCore.getCoreElement().addTask(optionsSendingOperationTask);
		trunkRegisterForRegisterTask.start();
		trunkRegisterForUnregisterTask.start();
		optionsSendingOperationTask.start();

	}

	private static void processRegisterExtension(boolean isRegister) {
		try {
			ArrayList<String> trunkExtenList = Converter.getKeyList(ServerCore.getCoreElement().getTrunkExtensionList());
			if (Objects.isNull(trunkExtenList)) {
				return;
			}
			for (int i = 0; i < trunkExtenList.size(); i++) {
				Extension trunkExtension = ServerCore.getCoreElement().getTrunkExtension(trunkExtenList.get(i));
				if (Objects.isNull(trunkExtension) || trunkExtension.isRegister() != isRegister) {
					continue;
				}
				RegisterService.register(trunkExtension);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void processOptionsExtension() {
		try {
			ArrayList<String> localExtenList = Converter.getKeyList(ServerCore.getCoreElement().getLocalExtensionList());
			if (Objects.isNull(localExtenList)) {
				return;
			}
			for (int i = 0; i < localExtenList.size(); i++) {
				Extension localExtension = ServerCore.getCoreElement().getLocalExtension(localExtenList.get(i));
				if (Objects.isNull(localExtension) || !localExtension.isRegister()) {
					continue;
				}
				OptionsService.pingExtension(localExtension);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
