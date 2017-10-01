package sipserver.com.service.transport;

import javax.sip.ClientTransaction;
import javax.sip.DialogTerminatedEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.service.util.ExceptionService;

public class TransportService extends Service {

	private static StackLogger logger = CommonLogger.getLogger(TransportService.class);

	public TransportService() {
		super(logger);

	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport, ServerTransaction serverTransaction) {
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
		// String callId = callIdHeader.getCallId();
		new Exception().printStackTrace();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ServerTransaction transaction = getServerTransaction(transport.getSipProvider(), requestEvent.getRequest());
					ExceptionService.checkNullObject(transaction);
					Service service = getInService(requestEvent.getRequest().getMethod());
					if (service == null) {
						return;
					}
					service.processRequest(requestEvent, transport, transaction);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();
	}

	private Service getInService(String method) {
		if (method.equals(Request.REGISTER)) {
			return ServerCore.getServerCore().getRegisterServiceIn();
		}
		if (method.equals(Request.INVITE) || method.equals(Request.CANCEL)) {
			return ServerCore.getServerCore().getInviteServiceIn();
		}
		if (method.equals(Request.OPTIONS)) {
			return ServerCore.getServerCore().getOptionsServiceIn();
		}
		if (method.equals(Request.BYE)) {
			return ServerCore.getServerCore().getInviteServiceEnd();
		}
		return null;
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
					if (cseqHeader.getMethod().equals(Request.INVITE)) {
						ServerCore.getServerCore().getInviteServiceOut().processResponse(responseEvent, transport);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void sendResponseMessage(ServerTransaction serverTransaction, Request request, int responseCode, String sdpContent) {
		try {
			if (request == null) {
				throw new Exception();
			}
			SipServerTransport sipServerTransport = ServerCore.getTransport(request);
			if (sipServerTransport == null) {
				throw new Exception();
			}
			Response response = sipServerTransport.getMessageFactory().createResponse(responseCode, request);
			if (sdpContent != null) {
				response.setContent(sdpContent.getBytes(), sipServerTransport.getHeaderFactory().createContentTypeHeader("application", "sdp"));
			}
			response.addHeader(sipServerTransport.getHeaderFactory().createAllowHeader(SipServerSharedProperties.allowHeaderValue));

			SipURI contactUrl = sipServerTransport.getAddressFactory().createSipURI("Anonymous", sipServerTransport.getHost());
			contactUrl.setPort(sipServerTransport.getPort());

			// Create the contact name address.
			SipURI contactURI = sipServerTransport.getAddressFactory().createSipURI("Anonymous1", sipServerTransport.getHost());
			contactURI.setPort(sipServerTransport.getPort());

			ContactHeader contactHeader = sipServerTransport.getHeaderFactory().createContactHeader(sipServerTransport.getAddressFactory().createAddress(contactURI));
			response.addHeader(contactHeader);

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
			ExceptionService.checkNullObject(clientTransaction);
			ExceptionService.checkNullObject(clientTransaction.getRequest());
			clientTransaction.sendRequest();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent event) {
		// TODO: Set Dialog Terminated
	}

	@Override
	public void processTimeout(TimeoutEvent timeoutEvent) {
		try {
			Transaction transaction;
			if (timeoutEvent.isServerTransaction()) {
				transaction = timeoutEvent.getServerTransaction();
				Service service = getInService(transaction.getRequest().getMethod());
				ExceptionService.checkNullObject(service);
				service.processTimeout(timeoutEvent);
			} else {
				transaction = timeoutEvent.getClientTransaction();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent terminatedEvent) {
		try {
			Transaction transaction;
			if (terminatedEvent.isServerTransaction()) {
				transaction = terminatedEvent.getServerTransaction();
				Service service = getInService(transaction.getRequest().getMethod());
				ExceptionService.checkNullObject(service);
			} else {
				transaction = terminatedEvent.getClientTransaction();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
