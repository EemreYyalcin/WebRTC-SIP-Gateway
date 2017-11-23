package sipserver.com.server.transport.ws;

import javax.sip.message.Message;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class SipMessageEncoder implements Encoder.Text<Message> {

	@Override
	public void init(EndpointConfig config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public String encode(Message message) throws EncodeException {
		return message.toString();
	}

}
