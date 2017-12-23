package sipserver.com.core.sip.server.auth;

import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.UserCredentials;
import javax.sip.ClientTransaction;
import sipserver.com.domain.Extension;

public class AccountManagerImpl implements AccountManager {

	private Extension extension;

	public AccountManagerImpl(Extension extension) {
		setExtension(extension);
	}

	public UserCredentials getCredentials(ClientTransaction challengedTransaction, String realm) {
		return new UserCredentialsImpl(getExtension().getExten(), "nist.gov", getExtension().getPass());
	}

	public Extension getExtension() {
		return extension;
	}

	private void setExtension(Extension extension) {
		this.extension = extension;
	}


}