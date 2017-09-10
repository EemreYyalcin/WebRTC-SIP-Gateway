package sipserver.com.executer.core;

import java.util.Properties;

import javax.sip.header.ViaHeader;
import javax.sip.message.Message;

import sipserver.com.domain.Extension;
import sipserver.com.parameter.ParamConstant.TransportType;
import sipserver.com.server.SipServerTransport;
import sipserver.com.server.transport.TCPTransport;
import sipserver.com.server.transport.UDPTransport;
import sipserver.com.service.invite.InviteServiceIn;
import sipserver.com.service.mgcp.IvrEndpointService;
import sipserver.com.service.register.RegisterServiceIn;
import sipserver.com.service.register.RegisterServiceOut;
import sipserver.com.service.timer.TimerService;
import sipserver.com.service.transport.TransportService;

public class ServerCore {

	// Core Element
	private static CoreElement coreElement;
	private static ServerCore serverCore;
	// Core Transport
	private UDPTransport udpTransport;
	private TCPTransport tcpTransport;

	// Core Service
	private RegisterServiceIn registerServiceIn;
	private RegisterServiceOut registerServiceOut;
	private InviteServiceIn inviteServiceIn;

	private IvrEndpointService ivrEndpointService;

	// Managment Service
	private TransportService transportService;

	// TimerService
	private TimerService timerService;

	public static SipServerTransport getTransport(Message message) {
		ViaHeader viaHeader = (ViaHeader) message.getHeader(ViaHeader.NAME);
		if (viaHeader == null) {
			return null;
		}
		if (viaHeader.getTransport().toLowerCase().equals(TransportType.UDP.toString().toLowerCase())) {
			return ServerCore.getServerCore().getUDPTransport();
		}
		if (viaHeader.getTransport().toLowerCase().equals(TransportType.TCP.toString().toLowerCase())) {
			return ServerCore.getServerCore().getTCPTransport();
		}
		return null;
	}

	public static SipServerTransport getTransport(TransportType transportType) {
		if (transportType == TransportType.UDP) {
			return ServerCore.getServerCore().getUDPTransport();
		}
		if (transportType == TransportType.TCP) {
			return ServerCore.getServerCore().getTCPTransport();
		}
		return null;
	}

	// Local Extension
	public Extension getLocalExtension(String exten) {
		return (Extension) getCoreElement().getLocalExtensionList().get(exten);
	}

	public void addLocalExtension(Extension extension) {
		getCoreElement().getLocalExtensionList().put(extension.getExten(), extension);
	}

	// localExtension end

	// remoteExtension
	public Extension getTrunkExtension(String exten) {
		return (Extension) getCoreElement().getTrunkExtensionList().get(exten);
	}

	public void addTrunkExtension(Extension extension) {
		getCoreElement().getTrunkExtensionList().put(extension.getExten(), extension);
	}

	public Properties getTrunkExtensionList() {
		return getCoreElement().getTrunkExtensionList();
	}

	// remoteExtension end

	public static void main(String[] args) {
		ServerCore.serverCore = new ServerCore();
		ServerCore.coreElement = new CoreElement();

		String host = ServerCore.coreElement.getLocalServerIp();
		int port = ServerCore.coreElement.getLocalSipPort();
		if (args != null && args.length > 0) {
			host = args[0];
			if (args.length > 1) {
				port = Integer.valueOf(args[1]);
				ServerCore.coreElement.setLocalSipPort(port);
			}
		}

		ServerCore.serverCore.setUDPTransport(new UDPTransport(host, port));
		ServerCore.serverCore.getUDPTransport().startListening();
		ServerCore.serverCore.setTimerService(new TimerService());
		ServerCore.serverCore.setRegisterServiceIn(new RegisterServiceIn());
		ServerCore.serverCore.setRegisterServiceOut(new RegisterServiceOut());
		ServerCore.serverCore.setInviteServiceIn(new InviteServiceIn());
		ServerCore.serverCore.setTransportService(new TransportService());
		ServerCore.serverCore.setIvrEndpointService(new IvrEndpointService());
	}

	public static ServerCore getServerCore() {
		return serverCore;
	}

	public UDPTransport getUDPTransport() {
		return udpTransport;
	}

	public void setUDPTransport(UDPTransport udpTransport) {
		this.udpTransport = udpTransport;
	}

	public static CoreElement getCoreElement() {
		return coreElement;
	}

	public TCPTransport getTCPTransport() {
		return tcpTransport;
	}

	public void setTCPTransport(TCPTransport tcpTransport) {
		this.tcpTransport = tcpTransport;
	}

	public TimerService getTimerService() {
		return timerService;
	}

	public void setTimerService(TimerService timerService) {
		this.timerService = timerService;
	}

	public RegisterServiceIn getRegisterServiceIn() {
		return registerServiceIn;
	}

	public void setRegisterServiceIn(RegisterServiceIn registerServiceIn) {
		this.registerServiceIn = registerServiceIn;
	}

	public RegisterServiceOut getRegisterServiceOut() {
		return registerServiceOut;
	}

	public void setRegisterServiceOut(RegisterServiceOut registerServiceOut) {
		this.registerServiceOut = registerServiceOut;
	}

	public TransportService getTransportService() {
		return transportService;
	}

	public void setTransportService(TransportService transportService) {
		this.transportService = transportService;
	}

	public InviteServiceIn getInviteServiceIn() {
		return inviteServiceIn;
	}

	public void setInviteServiceIn(InviteServiceIn inviteServiceIn) {
		this.inviteServiceIn = inviteServiceIn;
	}

	public IvrEndpointService getIvrEndpointService() {
		return ivrEndpointService;
	}

	public void setIvrEndpointService(IvrEndpointService ivrEndpointService) {
		this.ivrEndpointService = ivrEndpointService;
	}

}
