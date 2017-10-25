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
import sipserver.com.executer.sip.transaction.TransactionBuilder;
import sipserver.com.server.SipServerTransport;

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
				ClientTransaction clientTransaction = (ClientTransaction) ServerCore.getCoreElement().findTransaction(recieveAddress, callIdHeader.getCallId(), ClientTransaction.class);
				if (Objects.isNull(clientTransaction)) {
					logger.warn("ClientTransaction has not Found");
					return;
				}
				clientTransaction.processResponse((Response) message);
				if (logger.isTraceEnabled()) {
					logger.trace("Message Recieved  " + recieveAddress + ":" + recievePort + "\n\n" + message.toString());
				}
				return;
			}

			ServerTransaction serverTransaction = (ServerTransaction) ServerCore.getCoreElement().findTransaction(recieveAddress, callIdHeader.getCallId(), ServerTransaction.class);
			if (Objects.nonNull(serverTransaction)) {
				return;
			}
			serverTransaction = TransactionBuilder.createAndStartServerTransaction((Request) message, recieveAddress, recievePort, transport, callIdHeader.getCallId());
			if (logger.isTraceEnabled()) {
				logger.trace(message.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
