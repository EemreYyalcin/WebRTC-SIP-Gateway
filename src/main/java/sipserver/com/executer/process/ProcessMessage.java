package sipserver.com.executer.process;

import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import sipserver.com.server.SipServer;

public abstract class ProcessMessage {

	private SipServer sipServer;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;

	public abstract void processRequest(RequestEvent requestEvent);
	public abstract void processResponse(ResponseEvent responseEvent);

	public ProcessMessage(SipServer sipServer) throws PeerUnavailableException {
		setSipServer(sipServer);
		messageFactory = sipServer.getSipFactory().createMessageFactory();
		setHeaderFactory(sipServer.getSipFactory().createHeaderFactory());
	}

	public static ProcessMessage createProcess(Request request, SipServer sipServer) {
		try {
			System.out.println("createProcess: " + request.getMethod());
			if (request.getMethod().equals("REGISTER")) {
				return new RegisterProcess(sipServer);
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public SipServer getSipServer() {
		return sipServer;
	}
	public void setSipServer(SipServer sipServer) {
		this.sipServer = sipServer;
	}

	public MessageFactory getMessageFactory() {
		return messageFactory;
	}
	public HeaderFactory getHeaderFactory() {
		return headerFactory;
	}
	public void setHeaderFactory(HeaderFactory headerFactory) {
		this.headerFactory = headerFactory;
	}

}
