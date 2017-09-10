package sipserver.com.domain;

import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;

public class CallParameter {

	private String displayName;
	private String exten;

	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getExten() {
		return exten;
	}
	public void setExten(String exten) {
		this.exten = exten;
	}

	public static CallParameter getCallParameter(FromHeader fromHeader) {
		if (fromHeader == null) {
			return null;
		}
		CallParameter callParameter = new CallParameter();
		String uri = fromHeader.getAddress().getURI().toString().trim();
		String scheme = fromHeader.getAddress().getURI().getScheme();
		String[] parts = uri.split(";");
		if (parts.length <= 0) {
			return null;
		}
		uri = parts[0].substring(scheme.length() + 1);
		if (uri.indexOf(":") > 0) {
			uri = uri.split(":")[0];
		}

		if (uri.indexOf("@") < 0) {
			return null;
		}
		String[] userAndHost = uri.split("@");
		if (userAndHost.length < 2) {
			return null;
		}
		callParameter.setExten(userAndHost[0]);
		callParameter.setDisplayName(fromHeader.getAddress().getDisplayName());
		return callParameter;
	}

	public static CallParameter getCallParameter(ToHeader toHeader) {
		if (toHeader == null) {
			return null;
		}
		CallParameter callParameter = new CallParameter();
		String uri = toHeader.getAddress().getURI().toString().trim();
		String scheme = toHeader.getAddress().getURI().getScheme();
		String[] parts = uri.split(";");
		if (parts.length <= 0) {
			return null;
		}
		uri = parts[0].substring(scheme.length() + 1);
		if (uri.indexOf(":") > 0) {
			uri = uri.split(":")[0];
		}

		if (uri.indexOf("@") < 0) {
			return null;
		}
		String[] userAndHost = uri.split("@");
		if (userAndHost.length < 2) {
			return null;
		}
		callParameter.setExten(userAndHost[0]);
		callParameter.setDisplayName(toHeader.getAddress().getDisplayName());
		return callParameter;
	}

}
