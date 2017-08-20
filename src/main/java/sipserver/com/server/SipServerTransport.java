package sipserver.com.server;

import java.util.Properties;

import gov.nist.core.StackLogger;
import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import sipserver.com.message.Handler;
import sipserver.com.parameter.ParamConstant.TransportType;

public abstract class SipServerTransport extends SipAdapter {

	// Connection
	private int port = 5060;
	private String protocol = "udp";
	private String host = "192.168.1.106";

	// SipServer
	private SipStack sipStack;
	private SipFactory sipFactory = null;
	private SipProvider sipProvider = null;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;
	private AddressFactory addressFactory;
	private DigestServerAuthenticationHelper digestServerAuthentication;

	// Logger
	private StackLogger logger;

	public SipServerTransport(String host, int port, TransportType transportType, StackLogger logger) {
		setHost(host);
		setPort(port);
		setProtocol(transportType.toString().toLowerCase());
		setLogger(logger);
	}

	public boolean startListening() {
		try {
			final Properties defaultProperties = new Properties();
			defaultProperties.setProperty("javax.sip.STACK_NAME", "server");
			defaultProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "ERROR");
			defaultProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "server_debug.txt");
			defaultProperties.setProperty("gov.nist.javax.sip.SERVER_LOG", "server_log.txt");
			defaultProperties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
			defaultProperties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");
			if (System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
				defaultProperties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
			}
			setSipFactory(SipFactory.getInstance());
			getSipFactory().setPathName("gov.nist");
			setSipStack(getSipFactory().createSipStack(defaultProperties));
			getSipStack().start();
			ListeningPoint lp = getSipStack().createListeningPoint(getHost(), getPort(), getProtocol());
			setSipProvider(getSipStack().createSipProvider(lp));;
			getSipProvider().addSipListener(this);
			getLogger().logFatalError("SipServer Get Started");
			getLogger().logFatalError("IP:" + getHost() + ",port:" + getPort());

			setMessageFactory(getSipFactory().createMessageFactory());
			setHeaderFactory(getSipFactory().createHeaderFactory());
			setAddressFactory(getSipFactory().createAddressFactory());
			setDigestServerAuthentication(new DigestServerAuthenticationHelper());

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void stop() {
		getSipStack().stop();
	}

	public void processRequest(RequestEvent requestEvent) {
		Handler.addRequestMessage(requestEvent);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}


	public SipStack getSipStack() {
		return sipStack;
	}

	public void setSipStack(SipStack sipStack) {
		this.sipStack = sipStack;
	}

	public SipFactory getSipFactory() {
		return sipFactory;
	}

	public void setSipFactory(SipFactory sipFactory) {
		this.sipFactory = sipFactory;
	}

	public SipProvider getSipProvider() {
		return sipProvider;
	}

	public void setSipProvider(SipProvider sipProvider) {
		this.sipProvider = sipProvider;
	}

	public MessageFactory getMessageFactory() {
		return messageFactory;
	}

	public void setMessageFactory(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	public HeaderFactory getHeaderFactory() {
		return headerFactory;
	}

	public void setHeaderFactory(HeaderFactory headerFactory) {
		this.headerFactory = headerFactory;
	}

	public AddressFactory getAddressFactory() {
		return addressFactory;
	}

	public void setAddressFactory(AddressFactory addressFactory) {
		this.addressFactory = addressFactory;
	}

	public DigestServerAuthenticationHelper getDigestServerAuthentication() {
		return digestServerAuthentication;
	}

	public void setDigestServerAuthentication(DigestServerAuthenticationHelper digestServerAuthentication) {
		this.digestServerAuthentication = digestServerAuthentication;
	}

	public StackLogger getLogger() {
		return logger;
	}

	public void setLogger(StackLogger logger) {
		this.logger = logger;
	}

}
