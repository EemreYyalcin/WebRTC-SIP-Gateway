package sipserver.com.server;

import javax.sip.message.Message;
import javax.websocket.Session;

import com.noyan.Base;
import com.noyan.network.socket.ServerSocketAdapter;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;

public abstract class SipServerTransport implements ServerSocketAdapter, Base {

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
