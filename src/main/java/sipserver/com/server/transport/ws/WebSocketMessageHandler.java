package sipserver.com.server.transport.ws;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

import sipserver.com.executer.sip.Handler;
import sipserver.com.parameter.constant.Constant.TransportType;
import sipserver.com.server.SipServerTransport;

public class WebSocketMessageHandler implements MessageHandler.Whole<String> {

	private Session session;

	public WebSocketMessageHandler(Session session) {
		this.session = session;
	}

	@Override
	public void onMessage(String message) {
		Handler.processSipMessage(SipServerTransport.decodeSipMessage(message.getBytes()), TransportType.WS, session);
//		System.out.println("Message :" + message);
	}

}
