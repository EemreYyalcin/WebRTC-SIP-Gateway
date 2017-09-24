package sipserver.com.domain;

import java.util.UUID;

import sipserver.com.parameter.constant.ParamConstant.TransportType;
import sipserver.com.parameter.param.ExtensionParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.server.transport.TCPTransport;
import sipserver.com.server.transport.UDPTransport;

public class Extension {

	private String exten;
	private int expiresTime = 3600;
	private String host;
	private String displayName;
	private int port = 5060;
	private String pass;
	private boolean isRegister = false;
	private boolean isAlive = false;
	private TransportType transportType = TransportType.UDP;

	private ExtensionParam extensionParameter = new ExtensionParam();

	private String lock = UUID.randomUUID().toString();

	public Extension(String exten, String pass, String host, int port) {
		setExten(exten);
		setHost(host);
		setPass(pass);
		setPort(port);
	}

	public Extension(String exten, String pass, String host) {
		setExten(exten);
		setHost(host);
		setPass(pass);
	}

	public Extension(String exten, String pass) {
		setExten(exten);
		setPass(pass);
	}

	public Extension() {
	}

	public void keepRegistered() {
		getExtensionParameter().setKeepRegisteredFlag(true);
		synchronized (lock) {
			lock.notifyAll();
		}
		if (getExtensionParameter().isCheckRegister()) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (getExtensionParameter().isKeepRegisteredFlag()) {
					try {
						setRegister(true);
						getExtensionParameter().setKeepRegisteredFlag(false);
						synchronized (lock) {
							lock.wait(expiresTime * 1000);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				setRegister(false);
				getExtensionParameter().setCheckRegister(false);
			}
		}).start();
		getExtensionParameter().setCheckRegister(true);
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return "Extension [exten=" + exten + ", expiresTime=" + expiresTime + ", host=" + host + ", displayName=" + displayName + "]";
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
		return isRegister;
	}

	public void setRegister(boolean isRegister) {
		this.isRegister = isRegister;
		this.isAlive = isRegister;
	}

	public TransportType getTransportType() {
		return transportType;
	}

	public void setTransportType(SipServerTransport transport) {
		if (transport instanceof UDPTransport) {
			this.transportType = TransportType.UDP;
			return;
		}
		if (transport instanceof TCPTransport) {
			this.transportType = TransportType.TLS;
			return;
		}
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public ExtensionParam getExtensionParameter() {
		return extensionParameter;
	}

}
