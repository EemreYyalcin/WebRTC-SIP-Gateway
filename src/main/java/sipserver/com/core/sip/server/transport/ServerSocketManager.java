package sipserver.com.core.sip.server.transport;

import java.net.InetAddress;
import java.net.SocketAddress;

import sipserver.com.core.sip.server.transport.udp.UdpServerSocket;


public abstract class ServerSocketManager extends Thread {

	private boolean running = false;
	private byte[] buffer;
	private int bufferLength = 512;
	private ServerSocketAdapter serverSocketAdapter;

	public ServerSocketManager(ServerSocketAdapter serverSocketAdapter) {
		setServerSocketAdapter(serverSocketAdapter);
		setBufferLength(bufferLength);
	}

	protected abstract void listenSocket();

	public abstract void send(byte[] buffer, InetAddress address, int port);

	public abstract void send(byte[] buffer, SocketAddress socketAddress);

	public abstract int getPort();

	public abstract InetAddress getInetAddress();

	@Override
	public void run() {
		listenSocket();
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public ServerSocketAdapter getServerSocketAdapter() {
		return serverSocketAdapter;
	}

	public void setServerSocketAdapter(ServerSocketAdapter serverSocketAdapter) {
		this.serverSocketAdapter = serverSocketAdapter;
	}

	public int getBufferLength() {
		return bufferLength;
	}

	public void flushBuffer() {
		buffer = new byte[getBufferLength()];
	}

	public void setBufferLength(int bufferLength) {
		this.bufferLength = bufferLength;
		flushBuffer();
	}
	
	public static void main(String[] args) {
		try {
			ServerSocketAdapter serverSocketAdapter = new ServerSocketAdapter() {

				@Override
				public void processRecieveData(byte[] data, InetAddress recieveAddress, int recievePort) {
					System.out.println("Recieved Data:" + new String(data));
					System.out.println("Recieved Address:" + recieveAddress);
					System.out.println("Recieved Port:" + recievePort);

				}

				@Override
				public void processException(Exception exception) {
					exception.printStackTrace();
				}
			};

			UdpServerSocket udpServerSocket1 = new UdpServerSocket(serverSocketAdapter, InetAddress.getByName("192.168.1.107"), 1234, 1024);
			UdpServerSocket udpServerSocket2 = new UdpServerSocket(serverSocketAdapter, InetAddress.getByName("192.168.1.107"), 1235, 1024);
			udpServerSocket1.start();
			udpServerSocket2.start();
			udpServerSocket1.send("Hey! I'm socket1".getBytes(), InetAddress.getByName("192.168.1.107"), 1235);
			udpServerSocket2.send("Hey! I'm socket2".getBytes(), InetAddress.getByName("192.168.1.107"), 1234);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
