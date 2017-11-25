package sipserver.com.domain;

import java.util.Objects;

import javax.websocket.Session;

import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.parameter.constant.Constant.TransportType;

public class Extension {

	private String exten;
	private int expiresTime = 3600;
	private String address;
	private String displayName;
	private int port = SipServerSharedProperties.blankCode;
	private String pass;
	private TransportType transportType;
	private Session session;
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

	public boolean isRegister() {
		if (Objects.nonNull(getSession())) {
			return true;
		}

		if (Objects.isNull(registerTime)) {
			return false;
		}
		if (registerTime + expiresTime * 1000 > System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	public boolean isAlive() {
		if (Objects.nonNull(getSession())) {
			return true;
		}

		if (Objects.isNull(aliveTime)) {
			return false;
		}
		if (aliveTime + SipServerSharedProperties.optionsSendingIntervallForRegisterExten > System.currentTimeMillis()) {
			return true;
		}
		return false;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public TransportType getTransportType() {
		return transportType;
	}

	public void setTransportType(TransportType transportType) {
		this.transportType = transportType;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		if (Objects.isNull(session)) {
			return;
		}
		this.session = session;
		this.session.getUserProperties().put(Extension.class.getName(), this);
	}

}
