package sipserver.com.executer.core;

import java.net.InetAddress;
import java.util.Objects;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.configuration.GeneralConfiguration;
import com.mgcp.transport.MGCPTransportLayer;

import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.parameter.constant.Constant.TransportType;
import sipserver.com.server.SipServerTransport;
import sipserver.com.server.transport.udp.UDPTransport;
import sipserver.com.server.transport.ws.WebsocketListener;
import sipserver.com.service.control.ExtensionControlService;

public class ServerCore {

	// Core Element
	private static CoreElement coreElement;
	private static ServerCore serverCore;

	// Core Transport
	private UDPTransport udpTransport;
	private WebsocketListener websocketListenerTransport;

	public static void main(String[] args) throws Exception {
		gettinStarted(args);
	}

	public static void gettinStarted(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		// Logger.getRootLogger().addAppender(Log.createConsoleAppender(null));
		ServerCore.serverCore = new ServerCore();
		ServerCore.coreElement = new CoreElement();
		ServerCore.coreElement.setLocalServerAddress("192.168.1.108");
		ServerCore.coreElement.setLocalSipPort(5060);

		/*
		 * 
		 * Mgcp Configuration
		 * 
		 */

		if (SipServerSharedProperties.mediaServerActive) {
			ServerCore.coreElement.setMediaServerAddress(InetAddress.getByName("192.168.1.104"));
			MGCPTransportLayer.createAndStartMgcpTransportLayer(2727);
			MGCPTransportLayer.getMgcpTransportLayer().setMediaServerAddress(coreElement.getMediaServerAddress());
			MGCPTransportLayer.getMgcpTransportLayer().setMediaServerPort(2427);
			MGCPTransportLayer.getMgcpTransportLayer().setIvrEndpointID(GeneralConfiguration.ivrEndpointID);
			MGCPTransportLayer.getMgcpTransportLayer().setConferenceEndpointID(GeneralConfiguration.conferenceEndpointID);
			MGCPTransportLayer.getMgcpTransportLayer().setBridgeEndpointID(GeneralConfiguration.bridgeEndpointID);
		}

		////////////
		ServerCore.serverCore.setUDPTransport(new UDPTransport());
		ServerCore.serverCore.getTransport(TransportType.UDP).start();

		ServerCore.serverCore.setWebsocketListenerTransport(new WebsocketListener());
		ServerCore.serverCore.getTransport(TransportType.WS).start();

		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1001", "test1001"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1002", "test1002"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1003", "test1003"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1004", "test1004"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1005", "test1005"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1006", "test1006"));

		ExtensionControlService.beginControl();

		Logger.getLogger(ServerCore.class).info("Server Core Started");

	}

	public static ServerCore getServerCore() {
		return serverCore;
	}

	public SipServerTransport getTransport(TransportType transportType) {
		if (Objects.isNull(transportType)) {
			return null;
		}
		if (transportType.equals(TransportType.UDP)) {
			return udpTransport;
		} else if (transportType.equals(TransportType.WS)) {
			return websocketListenerTransport;
		}
		return null;
	}

	public void setUDPTransport(UDPTransport udpTransport) {
		this.udpTransport = udpTransport;
	}

	public static CoreElement getCoreElement() {
		return coreElement;
	}

	public void setWebsocketListenerTransport(WebsocketListener websocketListenerTransport) {
		this.websocketListenerTransport = websocketListenerTransport;
	}
}
