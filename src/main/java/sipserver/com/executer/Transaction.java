package sipserver.com.executer;

import java.util.EventObject;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.message.Request;
import javax.sip.message.Response;
import sipserver.com.server.SipServer;

public abstract class Transaction {

	private String callId;
	private SipServer sipServer;
	private RequestEvent requestEvent;
	private ResponseEvent responseEvent;
	private Request request;
	private ServerTransaction serverTransaction;
	private DigestServerAuthenticationHelper digestServerAuthentication;

	public Transaction(SipServer sipServer, String callId) {
		try {
			this.setSipServer(sipServer);
			setCallId(callId);
			setDigestServerAuthentication(new DigestServerAuthenticationHelper());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract void processRequest();
	public abstract void processResponse();

	public void processMessage(EventObject event) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (event instanceof RequestEvent) {
						requestEvent = (RequestEvent) event;
						responseEvent = null;
						processRequest();
					} else {
						requestEvent = null;
						responseEvent = (ResponseEvent) event;
						processResponse();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

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

	public RequestEvent getRequestEvent() {
		return requestEvent;
	}

	public void setRequestEvent(RequestEvent requestEvent) {
		this.requestEvent = requestEvent;
	}

	public ResponseEvent getResponseEvent() {
		return responseEvent;
	}

	public void setResponseEvent(ResponseEvent responseEvent) {
		this.responseEvent = responseEvent;
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
	}

	public DigestServerAuthenticationHelper getDigestServerAuthentication() {
		return digestServerAuthentication;
	}

	public void setDigestServerAuthentication(DigestServerAuthenticationHelper digestServerAuthentication) {
		this.digestServerAuthentication = digestServerAuthentication;
	}

}
