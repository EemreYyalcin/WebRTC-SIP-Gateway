package sipserver.com.executer;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import sipserver.com.executer.process.ProcessMessage;
import sipserver.com.server.SipServer;

public class Transaction {

	private String callId;
	private SipServer sipServer;
	private ProcessMessage processMessage;
	private RequestEvent requestEvent;
	private ResponseEvent responseEvent;

	public Transaction(SipServer sipServer, String callId) {
		this.sipServer = sipServer;
		setCallId(callId);
	}

	public void process() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (getRequestEvent() != null) {
						processMessage.processRequest(getRequestEvent());
						// return response for client
					} else {
						processMessage.processResponse(getResponseEvent());
					}
					setRequestEvent(null);
					setResponseEvent(null);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public boolean processRequestMessage(RequestEvent requestEvent) {
		try {
			if (processMessage == null) {
				processMessage = ProcessMessage.createProcess(requestEvent.getRequest(), sipServer);
			}
			if (processMessage == null) {
				return false;
			}
			if (getRequestEvent() != null) {
				return false;
			}
			setRequestEvent(requestEvent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean processResponseMessage(ResponseEvent responseEvent) {
		try {
			if (processMessage == null) {
				return false;
			}
			if (getResponseEvent() != null) {
				return false;
			}
			setRequestEvent(requestEvent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
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

}
