package sipserver.com.executer.sip;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import com.noyan.util.NullUtil;

import gov.nist.javax.sip.message.SIPMessage;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.executer.sip.transaction.Transaction;
import sipserver.com.executer.sip.transaction.TransactionBuilder;
import sipserver.com.server.SipServerTransport;

public class Handler {

	private static Logger logger = Logger.getLogger(Handler.class);

	public static void processSipMessage(SIPMessage message, SipServerTransport transport) {
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
				if (logger.isTraceEnabled()) {
					logger.trace("Message Recieved  " + clientTransaction.getAddress() + ":" + clientTransaction.getPort() + "\n\n" + message.toString());
				}
				return;
			}
			Request request = (Request) message;

			ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
			if (Objects.isNull(viaHeader)) {
				logger.warn("Sip Message has not ViaHeader. ");
				logger.warn(message.toString());
				return;
			}

			if (Objects.nonNull(transaction)) {
				if (request.getMethod().equals(Request.BYE)) {
					transaction.processByeOrCancelRequest(request);
					return;
				}
				if (request.getMethod().equals(Request.CANCEL)) {
					transaction.processByeOrCancelRequest(request);
					return;
				}
				if (request.getMethod().equals(Request.ACK)) {
					logger.trace("ACK Recieved " + request.getMethod());
					if (request.getMethod().equals(Request.INVITE)) {
						return;
					}
//					ServerCore.getCoreElement().removeTransaction(transaction.getCallId());
					return;
				}
				return;
			}

			ServerTransaction serverTransaction = TransactionBuilder.createServerTransaction(request, InetAddress.getByName(viaHeader.getHost()), viaHeader.getPort(), transport, callIdHeader.getCallId());
			if (NullUtil.isNull(serverTransaction)) {
				logger.trace("Ignored Message " + request.getMethod());
				return;
			}
			Objects.requireNonNull(serverTransaction);
			serverTransaction.processRequest();
			if (logger.isTraceEnabled()) {
				logger.trace(message.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(message.toString());
		}
	}

}
