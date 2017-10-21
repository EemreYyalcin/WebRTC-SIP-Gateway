package sipserver.com.server.transport;

import java.net.InetAddress;

import com.noyan.network.socket.udp.UdpServerSocket;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.Handler;
import sipserver.com.server.SipServerTransport;

public class UDPTransport extends SipServerTransport {

	private UdpServerSocket udpServerSocket;

	public UDPTransport() {
		udpServerSocket = new UdpServerSocket(this, ServerCore.getCoreElement().getLocalServerAddress(), ServerCore.getCoreElement().getLocalSipPort(), 1024);
	}

	@Override
	public void listen() {
		udpServerSocket.start();
		super.startListening();
	}

	@Override
	public void processRecieveData(byte[] data, InetAddress recieveAddress, int recievePort) {
		try {
			StringMsgParser smp = new StringMsgParser();
			SIPMessage sipMessage = smp.parseSIPMessage(data, true, false, null);
			Handler.processSipMessage(sipMessage, recieveAddress, recievePort, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processException(Exception exception) {
		exception.printStackTrace();
		udpServerSocket.setRunning(false);
	}

	@Override
	public void sendData(String data, InetAddress toAddress, int port) {
		udpServerSocket.send(data.getBytes(), toAddress, port);
	}

}
