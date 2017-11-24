package sipserver.com.server.transport.udp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.noyan.network.socket.udp.UdpServerSocket;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.Handler;
import sipserver.com.parameter.constant.Constant.TransportType;
import sipserver.com.server.SipServerTransport;

public class UDPTransport extends SipServerTransport {

	private UdpServerSocket udpServerSocket;

	public static UDPTransport createUdpTransport() {
		try {
			UDPTransport udpTransport = new UDPTransport();
			UdpServerSocket udpServerSocket = new UdpServerSocket(udpTransport, InetAddress.getByName(ServerCore.getCoreElement().getLocalServerAddress()), ServerCore.getCoreElement().getLocalSipPort(), 1024);
			udpTransport.setUdpServerSocket(udpServerSocket);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void listen() {
		getUdpServerSocket().start();
		super.startListening();
	}

	@Override
	public void processRecieveData(byte[] data, InetAddress recieveAddress, int recievePort) {
		try {
			StringMsgParser smp = new StringMsgParser();
			StringMsgParser.setComputeContentLengthFromMessage(true);
			SIPMessage sipMessage = smp.parseSIPMessage(data, true, false, null);
			Handler.processSipMessage(sipMessage, TransportType.UDP);
		} catch (Exception e) {
			String message = new String(data);
			if (message.startsWith("PUBLISH") || message.startsWith("SUBSCRIBE")) {
				return;
			}
			e.printStackTrace();
			error("Parse Exception " + e);
			error("Error Parse Message " + new String(data));
		}
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
	protected void sendData(String data, String toAddress, int port) {
		try {
			getUdpServerSocket().send(data.getBytes(), InetAddress.getByName(toAddress), port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
