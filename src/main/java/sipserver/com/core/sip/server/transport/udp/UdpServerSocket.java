package sipserver.com.core.sip.server.transport.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import sipserver.com.core.sip.server.transport.ServerSocketAdapter;
import sipserver.com.core.sip.server.transport.ServerSocketManager;

public class UdpServerSocket extends ServerSocketManager {

	private DatagramSocket socket;

	public UdpServerSocket(ServerSocketAdapter serverSocketAdapter, int port) {
		this(serverSocketAdapter, null, port);
	}

	public UdpServerSocket(ServerSocketAdapter serverSocketAdapter, InetAddress localAddress, int port) {
		this(serverSocketAdapter, localAddress, port, -1);
	}

	public UdpServerSocket(ServerSocketAdapter serverSocketAdapter, InetAddress localAddress, int port, int bufferLength) {
		super(serverSocketAdapter);
		try {
			if (Objects.isNull(localAddress)) {
				socket = new DatagramSocket(port);
			} else {
				socket = new DatagramSocket(port, localAddress);
			}

			if (bufferLength > 0) {
				setBufferLength(bufferLength);
			}
		} catch (Exception e) {
			getServerSocketAdapter().processException(e);
		}
	}

	@Override
	public void listenSocket() {
		setRunning(true);
		while (isRunning()) {
			try {
				DatagramPacket packet = new DatagramPacket(getBuffer(), getBufferLength());
				socket.receive(packet);
				CompletableFuture.runAsync(() -> {
					getServerSocketAdapter().processRecieveData(packet.getData(), packet.getAddress(), packet.getPort());
				});

				flushBuffer();
			} catch (Exception e) {
				getServerSocketAdapter().processException(e);
			}
		}
	}

	@Override
	public InetAddress getInetAddress() {
		if (Objects.isNull(socket)) {
			return null;
		}

		return socket.getLocalAddress();
	}

	@Override
	public int getPort() {
		if (Objects.isNull(socket)) {
			return -1;
		}

		return socket.getLocalPort();
	}

	@Override
	public void send(byte[] buffer, InetAddress address, int port) {
		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
			socket.send(packet);
		} catch (Exception e) {
			getServerSocketAdapter().processException(e);
		}
	}

	@Override
	public void send(byte[] buffer, SocketAddress socketAddress) {
		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, socketAddress);
			socket.send(packet);
		} catch (Exception e) {
			getServerSocketAdapter().processException(e);
		}
	}
}
