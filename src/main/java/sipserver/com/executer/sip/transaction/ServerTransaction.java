package sipserver.com.executer.sip.transaction;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.server.SipServerTransport;

public abstract class ServerTransaction extends Transaction {

	public abstract void processRequest();

	public ServerTransaction(Request request, InetAddress address, int port, SipServerTransport transport) {
		super(request, address, port, transport);
	}

	public void sendResponseMessage(int responseCode, String content) {
		try {
			Response response = getTransport().getMessageFactory().createResponse(responseCode, getRequest());
			if (Objects.nonNull(content)) {
				response.setContent(content.getBytes(), getTransport().getHeaderFactory().createContentTypeHeader("application", "sdp"));
			}
			response.addHeader(getTransport().getHeaderFactory().createAllowHeader(SipServerSharedProperties.allowHeaderValue));

			SipURI contactUrl = getTransport().getAddressFactory().createSipURI("Anonymous", ServerCore.getCoreElement().getLocalServerAddress().getHostAddress());
			contactUrl.setPort(ServerCore.getCoreElement().getLocalSipPort());

			// Create the contact name address.
			SipURI contactURI = getTransport().getAddressFactory().createSipURI("Anonymous1", ServerCore.getCoreElement().getLocalServerAddress().getHostAddress());
			contactURI.setPort(ServerCore.getCoreElement().getLocalSipPort());

			ContactHeader contactHeader = getTransport().getHeaderFactory().createContactHeader(getTransport().getAddressFactory().createAddress(contactURI));
			response.addHeader(contactHeader);

			if (getLogger().isTraceEnabled()) {
				trace(response.toString());
			}
			getTransport().sendData(response.toString(), getAddress(), getPort());
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
		getTransport().sendData(response.toString(), getAddress(), getPort());
	}

}
