package sipserver.com.domain;

import javax.sip.header.ContactHeader;

public class Extension {

	private String exten;
	private int expiresTime = 60;
	private long timeStamp;
	private String host;
	private String displayName;

	public Extension(String exten, String host) {
		setExten(exten);
		setHost(host);
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
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
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
		return "Extension [exten=" + exten + ", expiresTime=" + expiresTime + ", timeStamp=" + timeStamp + ", host=" + host + ", displayName=" + displayName + "]";
	}

}
