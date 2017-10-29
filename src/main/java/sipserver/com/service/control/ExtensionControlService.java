package sipserver.com.service.control;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.executer.task.TaskBuilder;
import sipserver.com.service.operational.OptionsService;
import sipserver.com.service.operational.RegisterService;
import sipserver.com.service.util.converter.Converter;

public class ExtensionControlService {

	private static Consumer<String> takeLog = (e) -> {
		if (Objects.nonNull(e) && e.length() > 0) {
			Logger.getLogger(ExtensionControlService.class).trace(e);
		}
	};

	public static void beginControl() {
		try {
			Consumer<String> registerSendingOperationForUnregisterTrunkExten = (e) -> {
				takeLog.accept(e);
				processRegisterExtension(false);
			};

			Consumer<String> registerSendingOperationForRegisterTrunkExten = (e) -> {
				takeLog.accept(e);
				processRegisterExtension(true);
			};

			Consumer<String> optionsSendingOperation = (e) -> {
				takeLog.accept(e);
				processOptionsExtension();
			};

			TaskBuilder.createAndStartTask(SipServerSharedProperties.registerSendingIntervallForUnRegisterExten, registerSendingOperationForUnregisterTrunkExten, "trunkRegisterForUnregisterTask");
			TaskBuilder.createAndStartTask(SipServerSharedProperties.registerSendingIntervallForRegisterExten, registerSendingOperationForRegisterTrunkExten, "trunkRegisterForRegisterTask");
			TaskBuilder.createAndStartTask(SipServerSharedProperties.optionsSendingIntervallForRegisterExten, optionsSendingOperation, "optionsSendingOperationTask");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(ExtensionControlService.class).error(e);
		}
	}

	private static void processRegisterExtension(boolean isRegister) {
		try {
			Stream<Object> localExtenStream = Converter.getKeyList(ServerCore.getCoreElement().getLocalExtensionList());
			if (Objects.isNull(localExtenStream)) {
				return;
			}

			localExtenStream.forEach(e -> {
				Extension trunkExtension = ServerCore.getCoreElement().getLocalExtension(e.toString());
				if (Objects.isNull(trunkExtension) || trunkExtension.isRegister() != isRegister || !trunkExtension.isTrunk() || !trunkExtension.isAuthenticatedTrunk()) {
					return;
				}
				RegisterService.register(trunkExtension);
			});

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(ExtensionControlService.class).error(e);
		}

	}

	private static void processOptionsExtension() {
		try {
			Stream<Object> localExtenStream = Converter.getKeyList(ServerCore.getCoreElement().getLocalExtensionList());
			if (Objects.isNull(localExtenStream)) {
				return;
			}

			localExtenStream.forEach(e -> {
				Extension localExtension = ServerCore.getCoreElement().getLocalExtension(e.toString());
				if (Objects.isNull(localExtension) || !localExtension.isRegister()) {
					return;
				}
				OptionsService.pingExtension(localExtension);
			});

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(ExtensionControlService.class).error(e);
		}
	}

}
