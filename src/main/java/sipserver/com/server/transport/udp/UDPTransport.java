package sipserver.com.server.transport.udp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.sip.message.Message;
import javax.websocket.Session;

import com.noyan.network.socket.udp.UdpServerSocket;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.Handler;
import sipserver.com.parameter.constant.Constant.TransportType;
import sipserver.com.server.SipServerTransport;

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
		Handler.processSipMessage(SipServerTransport.decodeSipMessage(data), TransportType.UDP);
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
