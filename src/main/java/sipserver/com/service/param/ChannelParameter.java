package sipserver.com.service.param;

import javax.sip.Transaction;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.SipServerSharedProperties;

public class ChannelParameter {

	private Transaction fromTransaction;
	private Transaction toTransaction;
	private Extension fromExtension;
	private Extension toExtension;
	private String fromSdpContent;
	private String toSdpContent;
	private int fromResponseCode = SipServerSharedProperties.errorResponseCode;
	private int toResponseCode = SipServerSharedProperties.errorResponseCode;
	private boolean fromIsRecievedResponse = false;
	private boolean toIsRecievedResponse = false;

	

	public boolean isFromIsRecievedResponse() {
		return fromIsRecievedResponse;
	}

	public void setFromIsRecievedResponse(boolean fromIsRecievedResponse) {
		this.fromIsRecievedResponse = fromIsRecievedResponse;
	}

	public boolean isToIsRecievedResponse() {
		return toIsRecievedResponse;
	}

	public void setToIsRecievedResponse(boolean toIsRecievedResponse) {
		this.toIsRecievedResponse = toIsRecievedResponse;
	}

	public int getFromResponseCode() {
		return fromResponseCode;
	}

	public void setFromResponseCode(int fromResponseCode) {
		this.fromResponseCode = fromResponseCode;
	}

	public int getToResponseCode() {
		return toResponseCode;
	}

	public void setToResponseCode(int toResponseCode) {
		this.toResponseCode = toResponseCode;
	}

	public Transaction getFromTransaction() {
		return fromTransaction;
	}

	public void setFromTransaction(Transaction fromTransaction) {
		this.fromTransaction = fromTransaction;
	}

	public Transaction getToTransaction() {
		return toTransaction;
	}

	public void setToTransaction(Transaction toTransaction) {
		this.toTransaction = toTransaction;
	}

	public Extension getFromExtension() {
		return fromExtension;
	}

	public void setFromExtension(Extension fromExtension) {
		this.fromExtension = fromExtension;
	}

	public Extension getToExtension() {
		return toExtension;
	}

	public void setToExtension(Extension toExtension) {
		this.toExtension = toExtension;
	}

	public String getFromSdpContent() {
		return fromSdpContent;
	}

	public void setFromSdpContent(String fromSdpContent) {
		this.fromSdpContent = fromSdpContent;
	}

	public String getToSdpContent() {
		return toSdpContent;
	}

	public void setToSdpContent(String toSdpContent) {
		this.toSdpContent = toSdpContent;
	}

}
