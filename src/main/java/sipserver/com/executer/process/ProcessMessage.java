package sipserver.com.executer.process;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.server.SipServer;

public abstract class ProcessMessage {

	private SipServer sipServer;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;
	private DigestServerAuthenticationHelper digestServerAuthentication;

	public abstract void processRequest(RequestEvent requestEvent);
	public abstract void processResponse(ResponseEvent responseEvent);

	public ProcessMessage(SipServer sipServer) throws Exception {
		setSipServer(sipServer);
		messageFactory = sipServer.getSipFactory().createMessageFactory();
		setHeaderFactory(sipServer.getSipFactory().createHeaderFactory());
		setDigestServerAuthentication(new DigestServerAuthenticationHelper());
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

	protected void sendResponseMessage(ServerTransaction serverTransaction, Request request, int responseCode) {
		try {
			if (serverTransaction == null || request == null) {
				throw new Exception();
			}
			Response response = getMessageFactory().createResponse(responseCode, request);
			if (response != null) {
				serverTransaction.sendResponse(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	public DigestServerAuthenticationHelper getDigestServerAuthentication() {
		return digestServerAuthentication;
	}
	public void setDigestServerAuthentication(DigestServerAuthenticationHelper digestServerAuthentication) {
		this.digestServerAuthentication = digestServerAuthentication;
	}

}
