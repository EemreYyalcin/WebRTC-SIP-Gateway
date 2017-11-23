package sipserver.com.executer.sip.transaction;

import java.util.Objects;

import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Response;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;

public abstract class ServerTransaction extends Transaction {

	public abstract void processRequest();

	public void sendResponseMessage(int responseCode, String content) {
		try {
			Response response = null;
			if (Objects.nonNull(content)) {
				response = getTransport().getMessageFactory().createResponse(responseCode, getRequest(), (ContentTypeHeader) getRequest().getHeader(ContentTypeHeader.NAME), content.getBytes());
				response.setContent(content.getBytes(), getTransport().getHeaderFactory().createContentTypeHeader("application", "sdp"));
			} else {
				response = getTransport().getMessageFactory().createResponse(responseCode, getRequest());
			}
			response.addHeader(getTransport().getHeaderFactory().createAllowHeader(SipServerSharedProperties.allowHeaderValue));

			String displayName = "Anonymous";
			if (Objects.nonNull(getExtension())) {
				displayName = getExtension().getExten();
			}

			// Create the contact name address.
			SipURI contactURI = getTransport().getAddressFactory().createSipURI(displayName, ServerCore.getCoreElement().getLocalServerAddress().getHostAddress());
			contactURI.setPort(ServerCore.getCoreElement().getLocalSipPort());

			ContactHeader contactHeader = getTransport().getHeaderFactory().createContactHeader(getTransport().getAddressFactory().createAddress(contactURI));
			response.addHeader(contactHeader);

			if (getLogger().isTraceEnabled()) {
				trace(response.toString());
			}
			getTransport().sendSipMessage(response, getAddress(), getPort(), getSession());
			setResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendResponseMessage(int responseCode) {
		sendResponseMessage(responseCode, null);
	}

	public void sendResponseMessage(Response response) {
		if (getLogger().isTraceEnabled()) {
			trace(response.toString());
		}
		getTransport().sendSipMessage(response, getAddress(), getPort(), getSession());
	}

}
