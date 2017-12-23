package sipserver.com.core.sip.service.control;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;

import sipserver.com.core.sip.builder.MessageBuilder;
import sipserver.com.core.sip.handler.options.OptionsClientMessageHandler;
import sipserver.com.core.sip.handler.register.RegisterClientMessageHandler;
import sipserver.com.core.sip.parameter.constant.Constant.TransportType;
import sipserver.com.domain.Extension;
import sipserver.com.executer.starter.ServerCore;
import sipserver.com.executer.starter.SipServerSharedProperties;
import sipserver.com.executer.task.TaskBuilder;
import sipserver.com.util.converter.Converter;

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
				Request requestMessage = MessageBuilder.createRegisterMessage(trunkExtension);
				RegisterClientMessageHandler registerClientMessageHandler = new RegisterClientMessageHandler(requestMessage, trunkExtension);
				ServerCore.getCoreElement().addHandler(((CallIdHeader) requestMessage.getHeader(CallIdHeader.NAME)).getCallId(), registerClientMessageHandler);

				registerClientMessageHandler.onTrying();
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
				if (localExtension.getTransportType() == TransportType.WS) {
					return;
				}
				if (Objects.isNull(localExtension) || !localExtension.isRegister()) {
					return;
				}
				Request requestMessage = MessageBuilder.createOptionsMessage(localExtension);
				OptionsClientMessageHandler optionsClientMessageHandler = new OptionsClientMessageHandler(requestMessage, localExtension);
				ServerCore.getCoreElement().addHandler(((CallIdHeader) requestMessage.getHeader(CallIdHeader.NAME)).getCallId(), optionsClientMessageHandler);
				optionsClientMessageHandler.onTrying();
			});

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(ExtensionControlService.class).error(e);
		}
	}

}
