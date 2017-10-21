package sipserver.com.domain;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderAddress;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;

public class Extension {

	private String exten;
	private int expiresTime = 3600;
	private InetAddress address;
	private String displayName;
	private int port = 5060;
	private String pass;
	private boolean isAlive = false;
	private SipServerTransport transport;
	private Long registerTime;

	public Extension(String exten, String pass, InetAddress address, int port) {
		setExten(exten);
		setPass(pass);
		setPort(port);
		setAddress(address);
	}

	public Extension(String exten, String pass, InetAddress address) {
		setExten(exten);
		setPass(pass);
		setAddress(address);
	}

	public Extension(String exten, String pass) {
		setExten(exten);
		setPass(pass);
	}

	public Extension() {
	}

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

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
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

	public static Extension getExtension(HeaderAddress headerAddress) {
		try {
			String uri = headerAddress.getAddress().getURI().toString().trim();
			String scheme = headerAddress.getAddress().getURI().getScheme();
			String[] parts = uri.split(";");
			if (parts.length <= 0) {
				throw new Exception();
			}
			uri = parts[0].substring(scheme.length() + 1);
			if (uri.indexOf(":") > 0) {
				uri = uri.split(":")[0];
			}

			if (uri.indexOf("@") < 0) {
				throw new Exception();
			}
			String[] userAndHost = uri.split("@");
			if (userAndHost.length < 2) {
				throw new Exception();
			}

			Extension extLocalOrTrunk = ServerCore.getCoreElement().getLocalExtension(userAndHost[0]);
			if (Objects.isNull(extLocalOrTrunk)) {
				extLocalOrTrunk = ServerCore.getCoreElement().getTrunkExtension(userAndHost[0]);
			}
			if (Objects.isNull(extLocalOrTrunk)) {
				return null;
			}

			extLocalOrTrunk.setAddress(InetAddress.getByName(userAndHost[1]));
			extLocalOrTrunk.setDisplayName(headerAddress.getAddress().getDisplayName());
			if (headerAddress instanceof ContactHeader) {
				int expiresTime = ((ContactHeader) headerAddress).getExpires();
				if (expiresTime != -1) {
					extLocalOrTrunk.setExpiresTime(expiresTime);
				}

			}
			return extLocalOrTrunk;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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

	public void unregister() {
		registerTime = null;
	}

}
