package sipserver.com.executer;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.server.SipServer;

public abstract class Transaction {

	private String callId;
	private SipServer sipServer;
	private Request request;
	private ServerTransaction serverTransaction;
	private ClientTransaction clientTransaction;
	private Dialog sipDialog;

	public Transaction(SipServer sipServer, String callId) {
		try {
			this.setSipServer(sipServer);
			setCallId(callId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void processRequest(RequestEvent requestEvent);
	protected abstract void processResponse(ResponseEvent responseEvent);
	public abstract void processRequestTransaction(RequestEvent requestEvent);
	public abstract void processResponseTransaction(ResponseEvent responseEvent);

	protected void sendResponseMessage(ServerTransaction serverTransaction, Request request, int responseCode) {
		try {
			if (serverTransaction == null || request == null) {
				throw new Exception();
			}
			Response response = getSipServer().getMessageFactory().createResponse(responseCode, request);
			if (response != null) {
				serverTransaction.sendResponse(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getCallId() {
		return callId;
	}
	public void setCallId(String callId) {
		this.callId = callId;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public SipServer getSipServer() {
		return sipServer;
	}

	public void setSipServer(SipServer sipServer) {
		this.sipServer = sipServer;
	}

	public ServerTransaction getServerTransaction() {
		return serverTransaction;
	}

	public void setServerTransaction(ServerTransaction serverTransaction) {
		this.serverTransaction = serverTransaction;
		setSipDialog(serverTransaction.getDialog());
	}

	public ClientTransaction getClientTransaction() {
		return clientTransaction;
	}

	public void setClientTransaction(ClientTransaction clientTransaction) {
		this.clientTransaction = clientTransaction;
	}

	public Dialog getSipDialog() {
		return sipDialog;
	}

	public void setSipDialog(Dialog sipDialog) {
		this.sipDialog = sipDialog;
	}

}
