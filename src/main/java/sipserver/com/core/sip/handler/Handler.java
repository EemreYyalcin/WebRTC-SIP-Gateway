package sipserver.com.core.sip.handler;

import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.noyan.util.NullUtil;

import gov.nist.javax.sip.message.SIPMessage;
import sipserver.com.core.sip.handler.invite.InviteServerMessageHandler;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.invite.InviteServerTransaction;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.executer.sip.transaction.Transaction;
import sipserver.com.executer.sip.transaction.TransactionBuilder;
import sipserver.com.parameter.constant.Constant.TransportType;

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
				Response response = (Response) message;
				if (Objects.isNull(transaction.getMessageHandler())) {
					throw new Exception();
				}

				if (response.getStatusCode() == Response.RINGING) {
					transaction.getMessageHandler().onRinging();
					return;
				}

				if (response.getStatusCode() == Response.BUSY_HERE) {
					transaction.getMessageHandler().onReject(response.getStatusCode());
					return;
				}

				if (response.getStatusCode() == Response.OK) {
					if (Objects.isNull(response.getRawContent())) {
						transaction.getMessageHandler().onOk(null);
						return;
					}
					transaction.getMessageHandler().onOk(new String(response.getRawContent()));
					return;
				}

				if (response.getStatusCode() < 300 && response.getStatusCode() > 200) {
					if (Objects.isNull(response.getRawContent())) {
						transaction.getMessageHandler().onOk(null);
						return;
					}
					transaction.getMessageHandler().onOk(new String(response.getRawContent()));
					return;
				}

				if (response.getStatusCode() > 300) {
					transaction.getMessageHandler().onReject(response.getStatusCode());
					return;
				}
				logger.error("Specific Response Message");
				return;

			}
			Request request = (Request) message;

			if (Objects.nonNull(transaction)) {
				if (request.getMethod().equals(Request.BYE)) {
					transaction.getMessageHandler().onBye(request);
					return;
				}
				if (request.getMethod().equals(Request.CANCEL)) {
					transaction.getMessageHandler().onCancel(request);
					return;
				}
				if (request.getMethod().equals(Request.ACK)) {
					transaction.getMessageHandler().onACK();
					return;
				}
				logger.error("Specific Request Message");
				return;
			}

			ServerTransaction serverTransaction = TransactionBuilder.createServerTransaction(request, transportType, session);
			if (NullUtil.isNull(serverTransaction)) {
				logger.trace("Ignored Message " + request.getMethod());
				return;
			}
			
			//TODO: Remove Transactions
			if (serverTransaction instanceof InviteServerTransaction) {
				if (Objects.isNull(session)) {
					///TODO: For UDP
				}else {
					serverTransaction.setMessageHandler(new InviteServerMessageHandler(request, session));										
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(message.toString());
		}
	}

}
