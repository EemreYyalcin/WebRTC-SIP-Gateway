package sipserver.com.server.transport.ws;

import java.util.Objects;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import sipserver.com.domain.Extension;

public class ProgrammaticEnpoint extends Endpoint {

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		System.out.println("Peer " + session.getId() + " connected");
		session.addMessageHandler(new WebSocketMessageHandler(session));
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("Peer " + session.getId() + " disconnected due to " + closeReason.getReasonPhrase());
		removeSession(session);

	}

	@Override
	public void onError(Session session, Throwable error) {
		System.out.println("Error communicating with peer " + session.getId() + ". Detail: " + error.getMessage());
		removeSession(session);
	}

	private void removeSession(Session session) {
		if (Objects.nonNull(session)) {
			Object extension = session.getUserProperties().get("EXTENSION");
			if (Objects.nonNull(extension)) {
				((Extension) extension).setSession(null);
			}
		}
	}

}