package sipserver.com.core.sip.server.transport.ws;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

import sipserver.com.core.sip.handler.Handler;
import sipserver.com.core.sip.server.SipServerTransport;

public class WebSocketMessageHandler implements MessageHandler.Whole<String> {

	private Session session;

	public WebSocketMessageHandler(Session session) {
		this.session = session;
	}

	@Override
	public void onMessage(String message) {
		Handler.processSipMessage(SipServerTransport.decodeSipMessage(message.getBytes()), session);
	}

}
