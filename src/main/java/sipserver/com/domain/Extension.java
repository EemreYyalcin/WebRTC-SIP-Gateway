package sipserver.com.domain;

import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;

import sipserver.com.parameter.ParamConstant.TransportType;
import sipserver.com.server.SipServerTransport;
import sipserver.com.server.transport.TCPTransport;
import sipserver.com.server.transport.UDPTransport;

public class Extension {

	private String exten;
	private int expiresTime = 60;
	private String host;
	private String displayName;
	private int port = 5060;
	private String pass;
	private boolean isRegister = false;
	private TransportType transportType = TransportType.UDP;

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

	public Extension(ContactHeader contactHeader) throws Exception {
		String uri = contactHeader.getAddress().getURI().toString().trim();
		String scheme = contactHeader.getAddress().getURI().getScheme();
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
		setExten(userAndHost[0]);
		setHost(userAndHost[1]);
		setDisplayName(contactHeader.getAddress().getDisplayName());
		if (contactHeader.getExpires() != -1) {
			setExpiresTime(contactHeader.getExpires());
		}
	}

	public Extension(FromHeader fromHeader) throws Exception {
		String uri = fromHeader.getAddress().getURI().toString().trim();
		String scheme = fromHeader.getAddress().getURI().getScheme();
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
		setExten(userAndHost[0]);
		setHost(userAndHost[1]);
		setDisplayName(fromHeader.getAddress().getDisplayName());
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

}
