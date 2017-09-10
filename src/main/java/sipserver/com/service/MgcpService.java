package sipserver.com.service;

import java.net.InetAddress;
import java.util.Properties;

import org.mobicents.protocols.mgcp.handlers.MessageHandler;
import org.mobicents.protocols.mgcp.stack.JainMgcpExtendedListener;
import org.mobicents.protocols.mgcp.stack.JainMgcpStackImpl;
import org.mobicents.protocols.mgcp.stack.JainMgcpStackProviderImpl;

import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import sipserver.com.executer.core.ServerCore;

public abstract class MgcpService implements JainMgcpExtendedListener {

	// Properties
	private Properties messageStack = new Properties();
	private Properties serviceStack = new Properties();

	private JainMgcpStackProviderImpl caProvider;

	private InetAddress caIPAddress = null;
	private JainMgcpStackImpl caStack = null;

	private EndpointIdentifier endpointID;

	private JainMgcpStackImpl stack;
	private MessageHandler handler;

	public MgcpService() {
		try {
			setStack(new JainMgcpStackImpl());
			setHandler(new MessageHandler(getStack()));
			setCaIPAddress(InetAddress.getByName(ServerCore.getCoreElement().getLocalServerIp()));
			setCaStack(new JainMgcpStackImpl(getCaIPAddress(), ServerCore.getCoreElement().getMediaClientPort()));
			setCaProvider((JainMgcpStackProviderImpl) getStack().createProvider());
			getCaProvider().addJainMgcpListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract void createConnection(String callID, Service service, byte[] sdpData);

	public JainMgcpStackProviderImpl getCaProvider() {
		return caProvider;
	}

	public void setCaProvider(JainMgcpStackProviderImpl caProvider) {
		this.caProvider = caProvider;
	}

	public InetAddress getCaIPAddress() {
		return caIPAddress;
	}

	public void setCaIPAddress(InetAddress caIPAddress) {
		this.caIPAddress = caIPAddress;
	}

	public JainMgcpStackImpl getCaStack() {
		return caStack;
	}

	public void setCaStack(JainMgcpStackImpl caStack) {
		this.caStack = caStack;
	}

	public JainMgcpStackImpl getStack() {
		return stack;
	}

	public void setStack(JainMgcpStackImpl stack) {
		this.stack = stack;
	}

	public MessageHandler getHandler() {
		return handler;
	}

	public void setHandler(MessageHandler handler) {
		this.handler = handler;
	}

	public EndpointIdentifier getEndpointID() {
		return endpointID;
	}

	public void setEndpointID(EndpointIdentifier endpointID) {
		this.endpointID = endpointID;
	}

	public String takeMessage(String transactionID) {
		String messageID = messageStack.getProperty(transactionID);
		if (messageID != null) {
			messageStack.remove(transactionID);
		}
		return messageID;
	}

	public void putMessage(String transactionID, String messageID) {
		messageStack.put(transactionID, messageID);
	}

	public Service takeService(String messageID) {
		Service service = (Service) serviceStack.get(messageID);
		if (service != null) {
			messageStack.remove(serviceStack);
		}
		return service;
	}

	public void putService(String messageID, Service service) {
		serviceStack.put(messageID, service);
	}

}
