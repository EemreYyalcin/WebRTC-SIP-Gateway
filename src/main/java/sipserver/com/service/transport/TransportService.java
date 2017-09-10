package sipserver.com.service.transport;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;

import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.Transaction;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;

public class TransportService extends Service {

	private int transactionTimeout = 2;

	private static StackLogger logger = CommonLogger.getLogger(TransportService.class);

	private Properties executions = new Properties();

	public TransportService() {
		super(logger);

	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport) {
		if (requestEvent == null) {
			return;
		}
		if (requestEvent.getRequest() == null) {
			return;
		}

		CallIdHeader callIdHeader = (CallIdHeader) requestEvent.getRequest().getHeader(CallIdHeader.NAME);
		if (callIdHeader == null) {
			return;
		}
		String callId = callIdHeader.getCallId();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (requestEvent.getRequest().getMethod().equals(Request.REGISTER)) {
						ServerCore.getServerCore().getRegisterServiceIn().processRequest(requestEvent, transport);
						return;
					}
					if (requestEvent.getRequest().getMethod().equals(Request.INVITE)) {
						ServerCore.getServerCore().getInviteServiceIn().processRequest(requestEvent, transport);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		if (responseEvent == null) {
			return;
		}
		if (responseEvent.getResponse() == null) {
			return;
		}

		CallIdHeader callIdHeader = (CallIdHeader) responseEvent.getResponse().getHeader(CallIdHeader.NAME);
		if (callIdHeader == null) {
			return;
		}
		String callId = callIdHeader.getCallId();
		executions.remove(callId);

		if (responseEvent.getResponse().getStatusCode() == Response.TRYING) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					CSeqHeader cseqHeader = (CSeqHeader) responseEvent.getResponse().getHeader(CSeqHeader.NAME);
					if (cseqHeader == null) {
						return;
					}
					if (cseqHeader.getMethod().equals(Request.REGISTER)) {
						ServerCore.getServerCore().getRegisterServiceOut().processResponse(responseEvent, transport);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void beginTask(String taskId, int timeout, Object transaction) {
		ServerCore.getServerCore().getTimerService().registerTask(taskId, timeout);
		executions.put(taskId, transaction);
	}

	@Override
	public void endTask(String taskId) {
		try {
			Transaction transaction = (Transaction) executions.get(taskId);
			if (transaction == null) {
				return;
			}
			if (transaction instanceof ClientTransaction) {
				((ClientTransaction) transaction).sendRequest();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendResponseMessage(ServerTransaction serverTransaction, Response response) {
		try {
			if (serverTransaction == null) {
				logger.logFatalError("ServerTransaction Null");
				return;
			}
			if (serverTransaction.getRequest() == null) {
				logger.logFatalError("Request Null");
				return;
			}

			CallIdHeader callIdHeader = (CallIdHeader) response.getHeader(CallIdHeader.NAME);
			if (callIdHeader == null) {
				logger.logFatalError("CallId Null");
				return;
			}
			serverTransaction.sendResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendRequestMessage(ClientTransaction clientTransaction) {
		try {
			if (clientTransaction == null) {
				logger.logFatalError("ServerTransaction Null");
				return;
			}
			if (clientTransaction.getRequest() == null) {
				logger.logFatalError("Request Null");
				return;
			}

			CallIdHeader callIdHeader = (CallIdHeader) clientTransaction.getRequest().getHeader(CallIdHeader.NAME);
			if (callIdHeader == null) {
				logger.logFatalError("CallId Null");
				return;
			}
			beginTask(callIdHeader.getCallId(), getTransactionTimeout(), clientTransaction);
			clientTransaction.sendRequest();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int getTransactionTimeout() {
		return transactionTimeout;
	}

	public void setTransactionTimeout(int transactionTimeout) {
		this.transactionTimeout = transactionTimeout;
	}

	@Override
	public void mediaServerEvents(JainMgcpResponseEvent jainmgcpresponseevent, String callID) {
		// TODO Auto-generated method stub

	}

}
