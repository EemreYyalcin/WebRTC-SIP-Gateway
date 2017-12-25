package sipserver.com.core.sip.server.transport.udp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.sip.message.Message;
import javax.websocket.Session;

import sipserver.com.core.sip.handler.Handler;
import sipserver.com.core.sip.server.SipServerTransport;
import sipserver.com.executer.starter.ServerCore;

public class UDPTransport extends SipServerTransport {

	private UdpServerSocket udpServerSocket;

	private UDPTransport() {

	}

	public static UDPTransport createAndStartUdpTransport() {
		try {
			UDPTransport udpTransport = new UDPTransport();
			UdpServerSocket udpServerSocket = new UdpServerSocket(udpTransport, InetAddress.getByName(ServerCore.getCoreElement().getLocalServerAddress()), ServerCore.getCoreElement().getLocalSipPort(), 1024);
			udpTransport.setUdpServerSocket(udpServerSocket);
			udpServerSocket.start();
			return udpTransport;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void processRecieveData(byte[] data, InetAddress recieveAddress, int recievePort) {
		Handler.processSipMessage(SipServerTransport.decodeSipMessage(data));
	}

	@Override
	public void processException(Exception exception) {
		exception.printStackTrace();
		getUdpServerSocket().setRunning(false);
	}

	private UdpServerSocket getUdpServerSocket() {
		return udpServerSocket;
	}

	private void setUdpServerSocket(UdpServerSocket udpServerSocket) {
		this.udpServerSocket = udpServerSocket;
	}

	@Override
	public void sendSipMessage(Message message, String toAddress, int port, Session session) {
		try {
			getUdpServerSocket().send(message.toString().getBytes(), InetAddress.getByName(toAddress), port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
