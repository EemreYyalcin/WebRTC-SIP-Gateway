package sipserver.com.executer.starter;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import sipserver.com.core.sip.handler.MessageHandler;
import sipserver.com.core.sip.parameter.param.CallParam;
import sipserver.com.domain.Extension;
import sipserver.com.executer.task.Task;
import sipserver.com.util.operation.MicroOperation;

public class CoreElement {

	private Properties localExtensionList = new Properties();

	private Properties messageHandlerList = new Properties();

	private ArrayList<Task> taskList = new ArrayList<Task>();

	private Properties bridgedElements = new Properties();

	private String localServerAddress;;
	private int localSipPort = 5060;
	private InetAddress mediaServerAddress;
	private int mediaServerPort = 2427;
	private int mediaClientPort = 2727;

	// SipMessage Creater Element
	private SipFactory sipFactory = null;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;
	private AddressFactory addressFactory;
	private DigestServerAuthenticationHelper digestServerAuthentication;

	public Properties getLocalExtensionList() {
		return localExtensionList;
	}

	public int getLocalSipPort() {
		return localSipPort;
	}

	public void setLocalSipPort(int localSipPort) {
		this.localSipPort = localSipPort;
	}

	public int getMediaServerPort() {
		return mediaServerPort;
	}

	public void setMediaServerPort(int mediaServerPort) {
		this.mediaServerPort = mediaServerPort;
	}

	public int getMediaClientPort() {
		return mediaClientPort;
	}

	public void setMediaClientPort(int mediaClientPort) {
		this.mediaClientPort = mediaClientPort;
	}

	public Extension getLocalExtension(String exten) {
		return (Extension) getLocalExtensionList().get(exten);
	}

	public void addLocalExtension(Extension extension) {
		getLocalExtensionList().put(extension.getExten(), extension);
	}

	public InetAddress getMediaServerAddress() {
		return mediaServerAddress;
	}

	public void setMediaServerAddress(InetAddress mediaServerAddress) {
		this.mediaServerAddress = mediaServerAddress;
	}

	public MessageHandler findHandler(String callId) {
		return (MessageHandler) messageHandlerList.get(callId);
	}

	public <T extends MessageHandler> void addHandler(String callId, T handler) {
		if (MicroOperation.isAnyNull(callId, handler)) {
			return;
		}
		messageHandlerList.put(callId, handler);
	}

	public MessageHandler removeHandler(String callId) {
		if (Objects.isNull(callId)) {
			return null;
		}
		return (MessageHandler) messageHandlerList.remove(callId);
	}

	public void addBridgeElement(CallParam fromCallParam, CallParam toCallParam) {
		bridgedElements.put(fromCallParam, toCallParam);
		bridgedElements.put(toCallParam, fromCallParam);
	}

	public CallParam getBridgeElement(CallParam anyCallParam) {
		return (CallParam) bridgedElements.get(anyCallParam);
	}

	public void removeBridging(CallParam anyCallParam) {
		CallParam otherElement = (CallParam) bridgedElements.remove(anyCallParam);
		if (Objects.isNull(otherElement)) {
			return;
		}
		bridgedElements.remove(otherElement);
	}

	public void addTask(Task task) {
		getTaskList().add(task);
	}

	public ArrayList<Task> getTaskList() {
		return taskList;
	}

	public String getLocalServerAddress() {
		return localServerAddress;
	}

	public void setLocalServerAddress(String localServerAddress) {
		this.localServerAddress = localServerAddress;
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
