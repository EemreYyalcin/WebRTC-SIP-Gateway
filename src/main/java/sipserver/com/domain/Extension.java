package sipserver.com.domain;

import java.net.InetAddress;
import java.util.Objects;

import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.server.SipServerTransport;

public class Extension {

	private String exten;
	private int expiresTime = 3600;
	private InetAddress address;
	private String displayName;
	private int port = 5060;
	private String pass;
	private SipServerTransport transport;
	private Long registerTime;
	private Long aliveTime;

	private boolean isTrunk = false;
	private boolean isAuthenticatedTrunk = false;

	public String getExten() {
		return exten;
	}

	public void setExten(String exten) {
		this.exten = exten;
	}

	public int getExpiresTime() {
		return expiresTime;
	}

	public void setExpiresTime(int expiresTime) {
		this.expiresTime = expiresTime;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public SipServerTransport getTransport() {
		return transport;
	}

	public void setTransport(SipServerTransport transport) {
		this.transport = transport;
	}

	public boolean isRegister() {
		if (Objects.isNull(registerTime)) {
			return false;
		}
		if (registerTime + expiresTime * 1000 > System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	public boolean isAlive() {
		if (Objects.isNull(aliveTime)) {
			return false;
		}
		if (aliveTime + SipServerSharedProperties.optionsSendingIntervallForRegisterExten > System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public void keepRegistered() {
		registerTime = System.currentTimeMillis();
	}

	public void keepAlive() {
		aliveTime = System.currentTimeMillis();
	}

	public void unregister() {
		registerTime = null;
	}

	public boolean isTrunk() {
		return isTrunk;
	}

	public void setIsTrunk(boolean isTrunk) {
		this.isTrunk = isTrunk;
	}

	public boolean isAuthenticatedTrunk() {
		return isAuthenticatedTrunk;
	}

	public void setAuthenticatedTrunkStatus(boolean isAuthenticatedTrunk) {
		this.isAuthenticatedTrunk = isAuthenticatedTrunk;
	}

}
