package sipserver.com.core.sip.server;

import javax.sip.message.Message;
import javax.websocket.Session;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;
import sipserver.com.core.sip.server.transport.ServerSocketAdapter;

public abstract class SipServerTransport implements ServerSocketAdapter {

	public abstract void sendSipMessage(Message message, String toAddress, int port, Session session);

	public static SIPMessage decodeSipMessage(byte[] data) {
		try {
			StringMsgParser smp = new StringMsgParser();
			StringMsgParser.setComputeContentLengthFromMessage(true);
			SIPMessage sipMessage = smp.parseSIPMessage(data, true, false, null);
			return sipMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
