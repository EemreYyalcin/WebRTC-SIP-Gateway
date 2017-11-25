package sipserver.com.executer.sip.transaction;

import java.util.Objects;

import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;

public abstract class ServerTransaction extends Transaction {

	public ServerTransaction(Extension extension) {
		super(extension);
	}

	public abstract void processRequest();

	public void sendResponseMessage(int responseCode, String content) {
		try {
			Response response = null;
			if (Objects.nonNull(content)) {
				response = ServerCore.getCoreElement().getMessageFactory().createResponse(responseCode, getRequest(), (ContentTypeHeader) getRequest().getHeader(ContentTypeHeader.NAME), content.getBytes());
				response.setContent(content.getBytes(), ServerCore.getCoreElement().getHeaderFactory().createContentTypeHeader("application", "sdp"));
			} else {
				response = ServerCore.getCoreElement().getMessageFactory().createResponse(responseCode, getRequest());
			}
			response.addHeader(ServerCore.getCoreElement().getHeaderFactory().createAllowHeader(SipServerSharedProperties.allowHeaderValue));

			String displayName = "Anonymous";
			if (Objects.nonNull(getExtension())) {
				displayName = getExtension().getExten();
			}

			// Create the contact name address.
			SipURI contactURI = ServerCore.getCoreElement().getAddressFactory().createSipURI(displayName, ServerCore.getCoreElement().getLocalServerAddress());
			contactURI.setPort(ServerCore.getCoreElement().getLocalSipPort());

			ContactHeader contactHeader = ServerCore.getCoreElement().getHeaderFactory().createContactHeader(ServerCore.getCoreElement().getAddressFactory().createAddress(contactURI));
			response.addHeader(contactHeader);

			sendResponseMessage(response);
			setResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendResponseMessage(int responseCode) {
		sendResponseMessage(responseCode, null);
	}

	public void sendResponseMessage(Response response) {
		if (response.getStatusCode() != Response.TRYING && response.getStatusCode() != Response.RINGING) {
			ServerCore.getCoreElement().removeTransaction(getCallId());
		}
		getTransport().sendSipMessage(response, getAddress(), getPort(), getSession());
	}

}
