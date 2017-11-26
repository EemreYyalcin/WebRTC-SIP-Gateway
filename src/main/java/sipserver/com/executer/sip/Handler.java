package sipserver.com.executer.sip;

import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.noyan.util.NullUtil;

import gov.nist.javax.sip.message.SIPMessage;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.executer.sip.transaction.Transaction;
import sipserver.com.executer.sip.transaction.TransactionBuilder;
import sipserver.com.parameter.constant.Constant.TransportType;
import sipserver.com.service.operational.BridgeService;

public class Handler {

	private static Logger logger = Logger.getLogger(Handler.class);

	public static void processSipMessage(SIPMessage message, TransportType transportType) {
		processSipMessage(message, transportType, null);
	}

	public static void processSipMessage(SIPMessage message, TransportType transportType, Session session) {
		try {
			if (Objects.isNull(message)) {
				logger.warn("Null Message Recieved");
				return;
			}

			CallIdHeader callIdHeader = (CallIdHeader) message.getHeader(CallIdHeader.NAME);
			if (Objects.isNull(callIdHeader)) {
				logger.warn("Sip Message has not CALLID. ");
				logger.warn(message.toString());
				return;
			}

			Transaction transaction = ServerCore.getCoreElement().findTransaction(callIdHeader.getCallId());
			if (message instanceof Response) {

				if (Objects.isNull(transaction)) {
					logger.warn("ClientTransaction has not Found");
					return;
				}
				if (transaction instanceof ServerTransaction) {
					// Bye Or Cancel Message Response
					logger.info("Bye Or Cancel Response Recieved !!");
					transaction.sendACK();
					return;
				}

				ClientTransaction clientTransaction = (ClientTransaction) transaction;
				clientTransaction.processResponse((Response) message);
				return;
			}
			Request request = (Request) message;

			if (Objects.nonNull(transaction)) {
				if (request.getMethod().equals(Request.BYE)) {
					BridgeService.observeTransaction(transaction, Response.REQUEST_TERMINATED);
					return;
				}
				if (request.getMethod().equals(Request.CANCEL)) {
					BridgeService.observeTransaction(transaction, Response.TEMPORARILY_UNAVAILABLE);
					return;
				}
				if (request.getMethod().equals(Request.ACK)) {
					logger.trace("ACK Recieved " + request.getMethod());
					transaction.processACK();
					return;
				}
				return;
			}

			ServerTransaction serverTransaction = TransactionBuilder.createServerTransaction(request, transportType, session);
			if (NullUtil.isNull(serverTransaction)) {
				logger.trace("Ignored Message " + request.getMethod());
				return;
			}
			serverTransaction.processRequest();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(message.toString());
		}
	}

}
