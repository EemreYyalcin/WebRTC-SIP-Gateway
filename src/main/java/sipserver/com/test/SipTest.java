package sipserver.com.test;

import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import sipserver.com.server.SipAdapter;

import java.util.Properties;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

/**
 * Test Issue 309 Via.setRPort() creates malformed rport parameter
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipTest {

	public final int SERVER_PORT = 5060;

	protected String testProtocol = "udp";

	public HeaderFactory headerFactory;

	public MessageFactory messageFactory;

	public AddressFactory addressFactory;

	public String host = "192.168.1.106";

	public void testRPort() throws Exception {
		Server server = new Server();
	}

	public class Server extends SipAdapter {
		protected SipStack sipStack;

		protected SipFactory sipFactory = null;

		protected SipProvider provider = null;

		private Request lastRequestReceived;

		public Server() {
			try {
				final Properties defaultProperties = new Properties();

				defaultProperties.setProperty("javax.sip.STACK_NAME", "server");
				defaultProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
				defaultProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "server_debug.txt");
				defaultProperties.setProperty("gov.nist.javax.sip.SERVER_LOG", "server_log.txt");
				defaultProperties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
				defaultProperties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");
				if (System.getProperty("enableNIO") != null
						&& System.getProperty("enableNIO").equalsIgnoreCase("true")) {
					defaultProperties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY",
							NioMessageProcessorFactory.class.getName());
				}
				this.sipFactory = SipFactory.getInstance();
				this.sipFactory.setPathName("gov.nist");
				this.sipStack = this.sipFactory.createSipStack(defaultProperties);
				this.sipStack.start();
				ListeningPoint lp = this.sipStack.createListeningPoint(host, SERVER_PORT, testProtocol);
				this.provider = this.sipStack.createSipProvider(lp);
				;
				this.provider.addSipListener(this);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public void stop() {
			this.sipStack.stop();
		}

		public void processRequest(RequestEvent requestEvent) {
			lastRequestReceived = requestEvent.getRequest();
			System.out.println("RECIEVED MESSAGE:" + lastRequestReceived.toString());
		}

		public Request getLastRequestReceived() {
			return lastRequestReceived;
		}
	}

	public static void main(String[] args) throws Exception {
		SipTest sipTest = new SipTest();
		sipTest.testRPort();
	}
}
