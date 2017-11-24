package sipserver.com.server.transport.ws;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;
import sipserver.com.executer.sip.Handler;
import sipserver.com.parameter.constant.Constant.TransportType;

public class WebSocketMessageHandler implements MessageHandler.Whole<String> {

	private Session session;

	public WebSocketMessageHandler(Session session) {
		this.session = session;
	}

	@Override
	public void onMessage(String message) {
		Handler.processSipMessage(decode(message), TransportType.WS, session);
		System.out.println("Message :" + message);
	}

	public SIPMessage decode(String str) {
		try {
			StringMsgParser smp = new StringMsgParser();
			StringMsgParser.setComputeContentLengthFromMessage(true);
			SIPMessage sipMessage = smp.parseSIPMessage(str.getBytes(), true, false, null);
			return sipMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
