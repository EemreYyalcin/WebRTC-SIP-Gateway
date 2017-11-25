package sipserver.com.executer.sip.register;

import java.util.Objects;

import javax.sip.header.ContactHeader;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.sip.transaction.ClientTransaction;

public class RegisterClientTransaction extends ClientTransaction {

	public RegisterClientTransaction(Extension extension) {
		super(extension);
	}

	@Override
	public void processResponse(Response response) {
		try {
			ContactHeader contactHeader = (ContactHeader) getRequest().getHeader(ContactHeader.NAME);
			Objects.requireNonNull(contactHeader);
			Extension trunkExtensionIncoming = ExtensionBuilder.getExtension(contactHeader);
			Objects.requireNonNull(trunkExtensionIncoming);
			int statusCode = response.getStatusCode();

			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				// TODO: Making unautherized
				// if (!AliasService.isHaveAuthenticateHeader(response)) {
				// return;
				// }
				// AuthenticationHelper authenticationHelper = new
				// AuthenticationHelperImpl(null, new
				// AccountManagerImpl(ServerCore.getCoreElement().getTrunkExtension(trunkExtensionLocal.getExten())),
				// getTransport().getHeaderFactory());
				// ClientTransaction clientTransaction =
				// authenticationHelper.handleChallenge(responseEvent.getResponse(),
				// responseEvent.getClientTransaction(), transport.getSipProvider(), 5, false);
				//
				// sendRequestMessage();
				return;
			}
			if (statusCode == Response.FORBIDDEN) {
				return;
			}
			setResponse(response);
			trunkExtensionIncoming.keepRegistered();
			sendACK();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
