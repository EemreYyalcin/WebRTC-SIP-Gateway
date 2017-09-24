package sipserver.com.service.transport;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;

public class TransportService extends Service {

	private static StackLogger logger = CommonLogger.getLogger(TransportService.class);

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
		// String callId = callIdHeader.getCallId();

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
					if (requestEvent.getRequest().getMethod().equals(Request.OPTIONS)) {
						ServerCore.getServerCore().getOptionsServiceIn().processRequest(requestEvent, transport);
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

			clientTransaction.sendRequest();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
