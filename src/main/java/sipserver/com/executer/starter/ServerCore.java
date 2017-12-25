package sipserver.com.executer.starter;

import javax.sip.SipFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import sipserver.com.core.sip.server.SipServerTransport;
import sipserver.com.core.sip.server.transport.udp.UDPTransport;
import sipserver.com.core.sip.server.transport.ws.WebsocketListener;
import sipserver.com.core.sip.service.control.ExtensionControlService;
import sipserver.com.domain.ExtensionBuilder;

public class ServerCore {

	// Core Element
	private static CoreElement coreElement;
	private static ServerCore serverCore;

	// Core Transport
	private UDPTransport udpTransport;
	private WebsocketListener websocketListenerTransport;

	// public static void main(String[] args) throws Exception {
	// gettinStarted(args);
	// }

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

		// if (SipServerSharedProperties.mediaServerActive) {
		// ServerCore.coreElement.setMediaServerAddress(InetAddress.getByName("192.168.1.104"));
		// MGCPTransportLayer.createAndStartMgcpTransportLayer(2727);
		// MGCPTransportLayer.getMgcpTransportLayer().setMediaServerAddress(coreElement.getMediaServerAddress());
		// MGCPTransportLayer.getMgcpTransportLayer().setMediaServerPort(2427);
		// MGCPTransportLayer.getMgcpTransportLayer().setIvrEndpointID(GeneralConfiguration.ivrEndpointID);
		// MGCPTransportLayer.getMgcpTransportLayer().setConferenceEndpointID(GeneralConfiguration.conferenceEndpointID);
		// MGCPTransportLayer.getMgcpTransportLayer().setBridgeEndpointID(GeneralConfiguration.bridgeEndpointID);
		// }

		////////////

		setSipCreaterSettings();

		// ServerCore.serverCore.setUDPTransport(UDPTransport.createAndStartUdpTransport());
		ServerCore.serverCore.setWebsocketListenerTransport(new WebsocketListener());

		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1001", "test1001"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1002", "test1002"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1003", "test1003"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1004", "test1004"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1005", "test1005"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1006", "test1006"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1007", "test1007"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1008", "test1008"));
		ServerCore.getCoreElement().addLocalExtension(ExtensionBuilder.createExtension("1009", "test1009"));

		ExtensionControlService.beginControl();

		Logger.getLogger(ServerCore.class).info("Server Core Started");

	}

	public static ServerCore getServerCore() {
		return serverCore;
	}

	public SipServerTransport getTransport(boolean isWS) {
		if (isWS) {
			return websocketListenerTransport;
		}
		return udpTransport;
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

	private static void setSipCreaterSettings() throws Exception {
		try {
			ServerCore.getCoreElement().setSipFactory(SipFactory.getInstance());
			ServerCore.getCoreElement().getSipFactory().setPathName("gov.nist");
			ServerCore.getCoreElement().setMessageFactory(ServerCore.getCoreElement().getSipFactory().createMessageFactory());
			ServerCore.getCoreElement().setHeaderFactory(ServerCore.getCoreElement().getSipFactory().createHeaderFactory());
			ServerCore.getCoreElement().setAddressFactory(ServerCore.getCoreElement().getSipFactory().createAddressFactory());
			ServerCore.getCoreElement().setDigestServerAuthentication(new DigestServerAuthenticationHelper());
		} catch (Exception e) {
			throw new Exception();
		}
	}

}
