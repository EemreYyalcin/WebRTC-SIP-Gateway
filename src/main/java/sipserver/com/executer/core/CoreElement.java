package sipserver.com.executer.core;

import java.util.Properties;

public class CoreElement {

	private Properties localExtensionList = new Properties();
	private Properties trunkExtensionList = new Properties();

	public Properties getLocalExtensionList() {
		return localExtensionList;
	}

	public void setLocalExtensionList(Properties localExtensionList) {
		this.localExtensionList = localExtensionList;
	}

	public Properties getTrunkExtensionList() {
		return trunkExtensionList;
	}

	public void setTrunkExtensionList(Properties trunkExtensionList) {
		this.trunkExtensionList = trunkExtensionList;
	}
	
}
