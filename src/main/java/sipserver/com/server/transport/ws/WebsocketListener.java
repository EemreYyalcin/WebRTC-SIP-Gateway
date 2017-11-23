package sipserver.com.server.transport.ws;

import java.net.InetAddress;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.noyan.util.log.Log;

import gov.nist.javax.sip.message.SIPMessage;
import sipserver.com.executer.sip.Handler;
import sipserver.com.server.SipServerTransport;

@ServerEndpoint(value = "/sipserver", encoders = { SipMessageEncoder.class }, decoders = { SipMessageDecoder.class })
public class WebsocketListener extends SipServerTransport {

	public WebsocketListener() {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		Logger.getRootLogger().addAppender(Log.createConsoleAppender(null));
		trace("Websocket Listening");
		System.out.println("Websocket Contructer Called");
	}

	@OnOpen
	public void onOpen(Session session) {
		try {
			debug("Websocket Connection Opened ");
			System.out.println("Websocket Connection Opened ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@OnMessage
	public void message(Session session, SIPMessage message) {
		try {
			Handler.processSipMessage(message, this);
			session.getBasicRemote().sendText(String.format("We received your message: %s%n", message));
			System.out.println("Message :" + message);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@OnError
	public void onError(Throwable e) {
		e.printStackTrace();
	}

	@OnClose
	public void onClose(Session session) {
		trace("Connection Closed " + session.getRequestURI());
		System.out.println("OnClose Called ");
	}

	@Override
	public void processRecieveData(byte[] data, InetAddress recieveAddress, int recievePort) {
		// TODO Auto-generated method stub
	}

	@Override
	public void processException(Exception exception) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void listen() {
		super.startListening();
	}

	@Override
	public void sendData(String data, InetAddress toAddress, int port) {
		// TODO Auto-generated method stub
	}

}
