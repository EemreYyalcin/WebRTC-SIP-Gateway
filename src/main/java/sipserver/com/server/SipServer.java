package sipserver.com.server;

import java.util.Properties;

import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import sipserver.com.executer.TransactionManager;
import sipserver.com.message.Handler;

public class SipServer extends SipAdapter {

	private int SERVER_PORT = 5060;

	private String protocol = "udp";

	private String host = "192.168.1.106";

	private SipStack sipStack;

	private SipFactory sipFactory = null;

	private SipProvider provider = null;

	private TransactionManager transactionManager;

	private Handler handler;
	
	

	public SipServer(String host, int port, String protocol) {
		this.SERVER_PORT = port;
		this.host = host;
		this.protocol = protocol;
	}

	public SipServer() {
	}

	public boolean startListening() {
		try {
			final Properties defaultProperties = new Properties();
			defaultProperties.setProperty("javax.sip.STACK_NAME", "server");
			defaultProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
			defaultProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "server_debug.txt");
			defaultProperties.setProperty("gov.nist.javax.sip.SERVER_LOG", "server_log.txt");
			defaultProperties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
			defaultProperties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");
			if (System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
				defaultProperties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
			}
			setSipFactory(SipFactory.getInstance());
			getSipFactory().setPathName("gov.nist");
			sipStack = getSipFactory().createSipStack(defaultProperties);
			sipStack.start();
			ListeningPoint lp = sipStack.createListeningPoint(host, SERVER_PORT, protocol);
			setProvider(sipStack.createSipProvider(lp));;
			getProvider().addSipListener(this);
			setTransactionManager(new TransactionManager(this));
			handler = new Handler(this);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void stop() {
		sipStack.stop();
	}

	public void processRequest(RequestEvent requestEvent) {
		handler.addRequestMessage(requestEvent);
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public static void main(String[] args) {
		SipServer sipServer = new SipServer();
		sipServer.startListening();
	}

	public SipFactory getSipFactory() {
		return sipFactory;
	}

	public void setSipFactory(SipFactory sipFactory) {
		this.sipFactory = sipFactory;
	}

	public SipProvider getProvider() {
		return provider;
	}

	public void setProvider(SipProvider provider) {
		this.provider = provider;
	}
}
