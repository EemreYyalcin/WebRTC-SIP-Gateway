package sipserver.com.executer.core;

import java.util.Properties;

public class CoreElement {

	private Properties localExtensionList = new Properties();
	private Properties trunkExtensionList = new Properties();

	
	private String localServerIp = "192.168.1.106";
	private int localSipPort = 5060;
	private String mediaServerIp = "192.168.1.105";
	private int mediaServerPort = 2427;
	private int mediaClientPort = 2727;
	
	

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

	public String getLocalServerIp() {
		return localServerIp;
	}

	public void setLocalServerIp(String localServerIp) {
		this.localServerIp = localServerIp;
	}

	public String getMediaServerIp() {
		return mediaServerIp;
	}

	public void setMediaServerIp(String mediaServerIp) {
		this.mediaServerIp = mediaServerIp;
	}

	public int getLocalSipPort() {
		return localSipPort;
	}

	public void setLocalSipPort(int localSipPort) {
		this.localSipPort = localSipPort;
	}

	public int getMediaServerPort() {
		return mediaServerPort;
	}

	public void setMediaServerPort(int mediaServerPort) {
		this.mediaServerPort = mediaServerPort;
	}

	public int getMediaClientPort() {
		return mediaClientPort;
	}

	public void setMediaClientPort(int mediaClientPort) {
		this.mediaClientPort = mediaClientPort;
	}

}
