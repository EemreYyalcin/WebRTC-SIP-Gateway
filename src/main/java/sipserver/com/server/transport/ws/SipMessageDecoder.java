package sipserver.com.server.transport.ws;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;

public class SipMessageDecoder implements Decoder.Text<SIPMessage> {

	@Override
	public void init(EndpointConfig config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public SIPMessage decode(String str) throws DecodeException {
		try {
			StringMsgParser smp = new StringMsgParser();
			StringMsgParser.setComputeContentLengthFromMessage(true);
			SIPMessage sipMessage = smp.parseSIPMessage(str.getBytes(), true, false, null);
			return sipMessage;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean willDecode(String s) {
		// TODO Auto-generated method stub
		return false;
	}

}
