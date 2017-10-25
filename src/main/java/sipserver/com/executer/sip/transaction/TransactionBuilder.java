package sipserver.com.executer.sip.transaction;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.invite.InviteClientTransaction;
import sipserver.com.executer.sip.invite.InviteServerTransaction;
import sipserver.com.executer.sip.options.OptionsClientTransaction;
import sipserver.com.executer.sip.options.OptionsServerTransaction;
import sipserver.com.executer.sip.register.RegisterClientTransaction;
import sipserver.com.executer.sip.register.RegisterServerTransaction;
import sipserver.com.server.SipServerTransport;

public class TransactionBuilder {

	private static Logger logger = Logger.getLogger(TransactionBuilder.class);

	public static ServerTransaction createAndStartServerTransaction(Request request, InetAddress address, int port, SipServerTransport transport, String callID) {
		try {
			Objects.requireNonNull(request);
			Objects.requireNonNull(address);
			Objects.requireNonNull(transport);
			ServerTransaction transaction = null;
			if (request.getMethod().equals(Request.REGISTER)) {
				transaction = new RegisterServerTransaction(request, address, port, transport);
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				transaction = new OptionsServerTransaction(request, address, port, transport);
			} else if (request.getMethod().equals(Request.INVITE)) {
				transaction = new InviteServerTransaction(request, address, port, transport);
			}
			if (Objects.isNull(transaction)) {
				logger.error("ServerTransaction Not Found Method:" + request.getMethod());
				return null;
			}
			ServerCore.getCoreElement().addTransaction(address, callID, transaction);
			if (logger.isTraceEnabled()) {
				logger.trace("Recieved Request :" + request.toString());
			}
			transaction.processRequest();
			return transaction;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ClientTransaction createAndStartClientTransaction(Request request, InetAddress address, int port, SipServerTransport transport) {
		try {
			Objects.requireNonNull(request);
			Objects.requireNonNull(address);
			Objects.requireNonNull(transport);
			CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
			Objects.requireNonNull(callIdHeader);
			ClientTransaction clientTransaction = (ClientTransaction) ServerCore.getCoreElement().findTransaction(address, callIdHeader.getCallId(), ClientTransaction.class);
			if (Objects.nonNull(clientTransaction)) {
				clientTransaction.sendRequestMessage();
				return clientTransaction;
			}
			if (request.getMethod().equals(Request.REGISTER)) {
				clientTransaction = new RegisterClientTransaction(request, address, port, transport);
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				clientTransaction = new OptionsClientTransaction(request, address, port, transport);
			} else if (request.getMethod().equals(Request.INVITE)) {
				clientTransaction = new InviteClientTransaction(request, address, port, transport);
			}

			if (Objects.isNull(clientTransaction)) {
				logger.error("ClientTransaction Not Found Method:" + request.getMethod());
				return null;
			}
			Objects.requireNonNull(clientTransaction);
			ServerCore.getCoreElement().addTransaction(address, callIdHeader.getCallId(), clientTransaction);
			if (logger.isTraceEnabled()) {
				logger.trace("Sending Request :" + request.toString());
			}
			clientTransaction.sendRequestMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
