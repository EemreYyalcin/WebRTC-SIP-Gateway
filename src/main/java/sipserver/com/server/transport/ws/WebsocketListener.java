package sipserver.com.server.transport.ws;

import java.io.IOException;
import java.net.InetAddress;

import javax.sip.message.Message;
import javax.websocket.Session;

import sipserver.com.server.SipServerTransport;

public class WebsocketListener extends SipServerTransport {

	@Override
	public void processRecieveData(byte[] data, InetAddress recieveAddress, int recievePort) {
		// TODO Auto-generated method stub
	}

	@Override
	public void processException(Exception exception) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendSipMessage(Message message, String toAddress, int port, Session session) {
		try {
			session.getBasicRemote().sendText(message.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
