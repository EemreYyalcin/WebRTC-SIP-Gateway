package sipserver.com.domain;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderAddress;
import javax.sip.header.ViaHeader;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;

public class ExtensionBuilder {

	public static Extension createExtension(String exten, String pass, String address, int port) {
		if (Objects.isNull(exten) || Objects.isNull(pass)) {
			return null;
		}
		Extension extension = new Extension();
		extension.setExten(exten);
		extension.setPass(pass);
		if (port != SipServerSharedProperties.blankCode) {
			extension.setPort(port);
		}
		extension.setAddress(address);
		return extension;
	}

	public static Extension createExtension(String exten, String pass) {
		return createExtension(exten, pass, null, SipServerSharedProperties.blankCode);
	}

	public static Extension createTrunkExtension(String exten, String pass, InetAddress address, int port, boolean isAuthenticatedTrunk) {
		Extension extension = createExtension(exten, pass, null, SipServerSharedProperties.blankCode);
		if (Objects.isNull(extension)) {
			return null;
		}
		extension.setIsTrunk(true);
		extension.setAuthenticatedTrunkStatus(isAuthenticatedTrunk);
		return extension;
	}

	public static Extension createTrunkExtension(String exten, String pass, boolean isAuthenticatedTrunk) {
		return createTrunkExtension(exten, pass, null, SipServerSharedProperties.blankCode, isAuthenticatedTrunk);
	}

	public static Extension getExtension(HeaderAddress headerAddress) {
		return getExtension(headerAddress, null);
	}

	public static Extension getExtension(HeaderAddress headerAddress, ViaHeader viaHeader) {
		try {
			Objects.requireNonNull(headerAddress);
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
				return null;
			}

			extLocalOrTrunk.setDisplayName(headerAddress.getAddress().getDisplayName());
			if (headerAddress instanceof ContactHeader) {
				int expiresTime = ((ContactHeader) headerAddress).getExpires();
				if (expiresTime != -1) {
					extLocalOrTrunk.setExpiresTime(expiresTime);
				}

			}
			if (Objects.nonNull(viaHeader)) {
				extLocalOrTrunk.setAddress(viaHeader.getHost());
				extLocalOrTrunk.setPort(viaHeader.getPort());
			}
			extLocalOrTrunk.keepAlive();
			return extLocalOrTrunk;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
