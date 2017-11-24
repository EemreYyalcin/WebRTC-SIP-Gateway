package sipserver.com.executer.core;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.Transaction;
import sipserver.com.executer.task.Task;

public class CoreElement {

	private Properties localExtensionList = new Properties();

	private Properties transactions = new Properties();

	private ArrayList<Task> taskList = new ArrayList<Task>();

	private String localServerAddress;;
	private int localSipPort = 5060;
	private InetAddress mediaServerAddress;
	private int mediaServerPort = 2427;
	private int mediaClientPort = 2727;

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

	public Transaction findTransaction(String callId) {
		return (Transaction) transactions.get(callId);
	}

	public <T extends Transaction> void addTransaction(String callId, T transaction) {
		Objects.requireNonNull(transaction);
		Objects.requireNonNull(callId);
		transactions.put(callId, transaction);
	}

	public Transaction removeTransaction(String callId) {
		Objects.requireNonNull(callId);
		return (Transaction) transactions.remove(callId);
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

}
