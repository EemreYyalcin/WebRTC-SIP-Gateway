package sipserver.com.executer.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import sipserver.com.domain.Extension;
import sipserver.com.server.transport.UDPTransport;
import sipserver.com.service.control.ExtensionControlService;

public class ServerCore {

	// Core Element
	private static CoreElement coreElement;
	private static ServerCore serverCore;

	// Core Transport
	private UDPTransport udpTransport;

	// Core Service

	// ControlService
	private ExtensionControlService extensionControlService;

	// Managment Service

	public static ArrayList<String> getExtenList(Properties properties) {
		try {
			if (properties == null) {
				return null;
			}
			ArrayList<String> extenList = null;
			synchronized (properties) {
				Set<Object> keys = properties.keySet();
				if (keys == null || keys.size() == 0) {
					return null;
				}
				for (Object key : keys) {
					if (extenList == null) {
						extenList = new ArrayList<String>();
					}
					extenList.add(new String((String) key));
				}
			}
			return extenList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws UnknownHostException {

		ServerCore.serverCore = new ServerCore();
		ServerCore.coreElement = new CoreElement();
		ServerCore.coreElement.setLocalServerAddress(InetAddress.getByName("192.168.1.107"));
		ServerCore.coreElement.setLocalSipPort(5060);

		if (args != null && args.length > 0) {
			ServerCore.coreElement.setLocalServerAddress(InetAddress.getByName(args[0]));
			if (args.length > 1) {
				ServerCore.coreElement.setLocalSipPort(Integer.valueOf(args[1]));
			}
		}

		ServerCore.serverCore.setUDPTransport(new UDPTransport());
		ServerCore.serverCore.getUDPTransport().startListening();
		ServerCore.serverCore.setExtensionControlService(new ExtensionControlService());

		ServerCore.getCoreElement().addLocalExtension(new Extension("1001", "test1001"));
		ServerCore.getCoreElement().addLocalExtension(new Extension("1002", "test1002"));
		ServerCore.getCoreElement().addLocalExtension(new Extension("1003", "test1003"));
		ServerCore.getCoreElement().addLocalExtension(new Extension("1004", "test1004"));
		ServerCore.getCoreElement().addLocalExtension(new Extension("1005", "test1005"));

		ExtensionControlService.beginControl();

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

	public ExtensionControlService getExtensionControlService() {
		return extensionControlService;
	}

	public void setExtensionControlService(ExtensionControlService extensionControlService) {
		this.extensionControlService = extensionControlService;
	}
}
