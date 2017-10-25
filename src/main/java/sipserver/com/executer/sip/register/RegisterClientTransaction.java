package sipserver.com.executer.sip.register;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.ContactHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.server.SipServerTransport;

public class RegisterClientTransaction extends ClientTransaction {

	public RegisterClientTransaction(Request request, InetAddress address, int port, SipServerTransport transport) {
		super(request, address, port, transport);
	}

	@Override
	public void processResponse(Response response) {
		try {
			ContactHeader contactHeader = (ContactHeader) getRequest().getHeader(ContactHeader.NAME);
			Objects.requireNonNull(contactHeader);
			Extension trunkExtensionIncoming = Extension.getExtension(contactHeader);
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
			trunkExtensionIncoming.keepRegistered();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
