package sipserver.com.service.control;

import java.util.ArrayList;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;

public class ExtensionControlService extends Thread {

	private int currentRegisterSendingIntervallForUnRegisterExten = 0;

	private int currentRegisterSendingIntervallForRegisterExten = 0;

	@Override
	public void run() {
		try {
			while (true) {
				try {
					if (currentRegisterSendingIntervallForUnRegisterExten > SipServerSharedProperties.registerSendingIntervallForUnRegisterExten) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								processUnRegisterExtension();
								currentRegisterSendingIntervallForUnRegisterExten = 0;
							}
						}).start();
					}

					if (currentRegisterSendingIntervallForRegisterExten > SipServerSharedProperties.registerSendingIntervallForRegisterExten) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								processRegisterExtension();
								currentRegisterSendingIntervallForRegisterExten = 0;
							}
						}).start();
					}

					updateCurrentInterval();
					Thread.sleep(SipServerSharedProperties.extensionControlServiceControlInterval);
				} catch (Exception e) {
					Thread.sleep(SipServerSharedProperties.extensionControlServiceControlInterval);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void processUnRegisterExtension() {
		try {
			ArrayList<String> trunkExtenList = ServerCore.getExtenList(ServerCore.getServerCore().getTrunkExtensionList());
			if (trunkExtenList == null) {
				return;
			}
			for (int i = 0; i < trunkExtenList.size(); i++) {
				Extension trunkExtension = ServerCore.getServerCore().getTrunkExtension(trunkExtenList.get(i));
				if (trunkExtension == null || trunkExtension.isRegister()) {
					continue;
				}
				callRegisterService(trunkExtension);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void processRegisterExtension() {
		try {
			ArrayList<String> trunkExtenList = ServerCore.getExtenList(ServerCore.getServerCore().getTrunkExtensionList());
			if (trunkExtenList == null) {
				return;
			}
			for (int i = 0; i < trunkExtenList.size(); i++) {
				Extension trunkExtension = ServerCore.getServerCore().getTrunkExtension(trunkExtenList.get(i));
				if (trunkExtension == null || !trunkExtension.isRegister()) {
					continue;
				}
				callRegisterService(trunkExtension);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void callRegisterService(Extension trunkExtension) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ServerCore.getServerCore().getRegisterServiceOut().register(trunkExtension);
			}
		}).start();
	}

	private void updateCurrentInterval() {
		currentRegisterSendingIntervallForRegisterExten += SipServerSharedProperties.extensionControlServiceControlInterval;
		currentRegisterSendingIntervallForUnRegisterExten += SipServerSharedProperties.extensionControlServiceControlInterval;
	}

}
