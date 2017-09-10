package sipserver.com.service.invite;

import javax.sip.ClientTransaction;
import javax.sip.ServerTransaction;

import sipserver.com.domain.Extension;

public class ChannelParameter {

	private ServerTransaction serverTransaction;
	private ClientTransaction clientTransaction;
	private Extension extension;

	public ServerTransaction getServerTransaction() {
		return serverTransaction;
	}

	public void setServerTransaction(ServerTransaction serverTransaction) {
		this.serverTransaction = serverTransaction;
	}

	public ClientTransaction getClientTransaction() {
		return clientTransaction;
	}

	public void setClientTransaction(ClientTransaction clientTransaction) {
		this.clientTransaction = clientTransaction;
	}

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	

}
