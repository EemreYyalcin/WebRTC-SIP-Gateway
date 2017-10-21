package sipserver.com.executer.core;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.executer.sip.transaction.Transaction;
import sipserver.com.executer.task.Task;

public class CoreElement {

	private Properties localExtensionList = new Properties();
	private Properties trunkExtensionList = new Properties();

	private Properties serverTransactionIp = new Properties();
	private Properties clientTransactionIp = new Properties();

	private ArrayList<Task> taskList = new ArrayList<Task>();

	private Function<Class<?>, Properties> getTransactionIp = (e) -> {
		if (e.isAssignableFrom(ServerTransaction.class)) {
			return serverTransactionIp;
		}
		return clientTransactionIp;
	};

	private InetAddress localServerAddress;;
	private int localSipPort = 5060;
	private InetAddress mediaServerAddress;
	private int mediaServerPort = 2427;
	private int mediaClientPort = 2727;

	public Properties getLocalExtensionList() {
		return localExtensionList;
	}

	public Properties getTrunkExtensionList() {
		return trunkExtensionList;
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

	public Extension getTrunkExtension(String exten) {
		return (Extension) getTrunkExtensionList().get(exten);
	}

	public void addTrunkExtension(Extension extension) {
		getTrunkExtensionList().put(extension.getExten(), extension);
	}

	public InetAddress getLocalServerAddress() {
		return localServerAddress;
	}

	public void setLocalServerAddress(InetAddress localServerAddress) {
		this.localServerAddress = localServerAddress;
	}

	public InetAddress getMediaServerAddress() {
		return mediaServerAddress;
	}

	public void setMediaServerAddress(InetAddress mediaServerAddress) {
		this.mediaServerAddress = mediaServerAddress;
	}

	public Transaction findTransaction(InetAddress address, String callId, Class<?> type) {
		Optional<Properties> transactionForCseq = Optional.ofNullable((Properties) getTransactionIp.apply(type).get(address));
		if (!transactionForCseq.isPresent()) {
			return null;
		}
		return (Transaction) transactionForCseq.get().get(callId);
	}

	public <T extends Transaction> void addTransaction(InetAddress address, String callId, T transaction) {
		Objects.requireNonNull(transaction);
		Objects.requireNonNull(address);
		Objects.requireNonNull(callId);
		Optional<Properties> transactionForCseq = Optional.ofNullable(getTransactionIp.apply(transaction.getClass()));
		transactionForCseq.orElseGet(() -> new Properties());
		transactionForCseq.get().put(callId, transaction);
		getTransactionIp.apply(transaction.getClass()).put(address, transactionForCseq);
	}

	public <T extends Transaction> T removeTransaction(InetAddress address, String callId, Class<T> type) {
		Objects.requireNonNull(address);
		Objects.requireNonNull(callId);
		Optional<Properties> transactionForCseq = Optional.ofNullable(getTransactionIp.apply(type));
		if (!transactionForCseq.isPresent()) {
			return null;
		}
		return type.cast(transactionForCseq.get().remove(callId));
	}

	public void addTask(Task task) {
		getTaskList().add(task);
	}

	public ArrayList<Task> getTaskList() {
		return taskList;
	}

}
