package sipserver.com.executer.sip;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import gov.nist.javax.sip.message.SIPMessage;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.executer.sip.transaction.Transaction;
import sipserver.com.executer.sip.transaction.TransactionBuilder;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.control.ChannelControlService;

public class Handler {

	private static Logger logger = Logger.getLogger(Handler.class);

	public static void processSipMessage(SIPMessage message, InetAddress recieveAddress, int recievePort, SipServerTransport transport) {
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

			if (message instanceof Response) {

				Transaction transaction = ServerCore.getCoreElement().findTransaction(callIdHeader.getCallId());
				if (Objects.isNull(transaction)) {
					logger.warn("ClientTransaction has not Found");
					return;
				}

				if (transaction instanceof ServerTransaction) {
					// Bye Or Cancel Message Response
					logger.info("Bye Or Cancel Response Recieved !!");
					ChannelControlService.takeChannel(transaction.getCallId());
					return;
				}

				ClientTransaction clientTransaction = (ClientTransaction) transaction;
				clientTransaction.processResponse((Response) message);
				if (logger.isTraceEnabled()) {
					logger.trace("Message Recieved  " + recieveAddress + ":" + recievePort + "\n\n" + message.toString());
				}
				return;
			}
			Request request = (Request) message;
			Transaction transaction = ServerCore.getCoreElement().findTransaction(callIdHeader.getCallId());
			if (Objects.nonNull(transaction)) {
				if (request.getMethod().equals(Request.BYE)) {
					transaction.processByeOrCancelRequest(request);
					return;
				}
				if (request.getMethod().equals(Request.CANCEL)) {
					((ServerTransaction) transaction).processByeOrCancelRequest(request);
					return;
				}
				if (request.getMethod().equals(Request.ACK)) {
					logger.trace("ACK Recieved " + request.getMethod());
					return;
				}
				return;
			}

			TransactionBuilder.createAndStartServerTransaction(request, recieveAddress, recievePort, transport, callIdHeader.getCallId());
			if (logger.isTraceEnabled()) {
				logger.trace(message.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(message.toString());
			logger.error(recieveAddress);
			logger.error(recievePort);
		}
	}

}
