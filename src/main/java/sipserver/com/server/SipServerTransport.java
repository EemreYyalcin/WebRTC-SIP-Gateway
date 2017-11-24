package sipserver.com.server;

import java.io.IOException;
import java.util.Objects;

import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Message;
import javax.sip.message.MessageFactory;
import javax.websocket.Session;

import com.noyan.Base;
import com.noyan.network.socket.ServerSocketAdapter;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;

public abstract class SipServerTransport extends Thread implements ServerSocketAdapter, Base {

	// SipServer
	private SipFactory sipFactory = null;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;
	private AddressFactory addressFactory;
	private DigestServerAuthenticationHelper digestServerAuthentication;

	protected abstract void listen();

	protected abstract void sendData(String data, String toAddress, int port);

	public void sendSipMessage(Message sipMessage, String toAddress, int port, Session session) {
		if (Objects.isNull(sipMessage)) {
			return;
		}
		if (Objects.isNull(session)) {
			sendData(sipMessage.toString(), toAddress, port);
			return;
		}
		try {
			session.getBasicRemote().sendText(sipMessage.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		listen();
	}

	public void startListening() {
		try {
			setSipFactory(SipFactory.getInstance());
			getSipFactory().setPathName("gov.nist");
			setMessageFactory(getSipFactory().createMessageFactory());
			setHeaderFactory(getSipFactory().createHeaderFactory());
			setAddressFactory(getSipFactory().createAddressFactory());
			setDigestServerAuthentication(new DigestServerAuthenticationHelper());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SipFactory getSipFactory() {
		return sipFactory;
	}

	public void setSipFactory(SipFactory sipFactory) {
		this.sipFactory = sipFactory;
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

}
