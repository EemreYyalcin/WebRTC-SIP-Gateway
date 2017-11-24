package sipserver.com.server.transport.ws;

import java.net.InetAddress;

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
	protected void listen() {
		super.startListening();
	}

	@Override
	public void sendData(String data, String toAddress, int port) {

	}

}
