package sipserver.com.executer.sip.transaction;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;

import sipserver.com.domain.Extension;
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
			if (Objects.isNull(request) || Objects.isNull(address) || Objects.isNull(transport) || Objects.isNull(callID)) {
				return null;
			}
			
			ServerTransaction serverTransaction = null;
			if (request.getMethod().equals(Request.REGISTER)) {
				serverTransaction = new RegisterServerTransaction();
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				serverTransaction = new OptionsServerTransaction();
			} else if (request.getMethod().equals(Request.INVITE)) {
				serverTransaction = new InviteServerTransaction();
			}
			if (Objects.isNull(serverTransaction)) {
				// logger.error("ServerTransaction Not Found Method:" + request.getMethod());
				return null;
			}
			serverTransaction.setRequest(request);
			serverTransaction.setAddress(address);
			serverTransaction.setPort(port);
			serverTransaction.setTransport(transport);
			serverTransaction.setCallId(callID);

			ServerCore.getCoreElement().addTransaction(callID, serverTransaction);

			if (logger.isTraceEnabled()) {
				logger.trace("Recieved Request :" + request.toString());
			}
			serverTransaction.processRequest();
			return serverTransaction;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ClientTransaction createAndStartClientTransaction(Request request, Extension toExten) {
		try {

			if (Objects.isNull(request) || Objects.isNull(toExten.getAddress()) || Objects.isNull(toExten.getTransport())) {
				return null;
			}

			CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
			if (Objects.isNull(callIdHeader)) {
				return null;
			}

			ClientTransaction clientTransaction = (ClientTransaction) ServerCore.getCoreElement().findTransaction(callIdHeader.getCallId());
			if (Objects.nonNull(clientTransaction)) {
				clientTransaction.sendRequestMessage();
				return clientTransaction;
			}

			if (request.getMethod().equals(Request.REGISTER)) {
				clientTransaction = new RegisterClientTransaction();
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				clientTransaction = new OptionsClientTransaction();
			} else if (request.getMethod().equals(Request.INVITE)) {
				clientTransaction = new InviteClientTransaction();
			}

			if (Objects.isNull(clientTransaction)) {
				logger.error("ClientTransaction Not Found Method:" + request.getMethod());
				return null;
			}

			clientTransaction.setRequest(request);
			clientTransaction.setAddress(toExten.getAddress());
			clientTransaction.setPort(toExten.getPort());
			clientTransaction.setTransport(toExten.getTransport());
			clientTransaction.setCallId(callIdHeader.getCallId());
			clientTransaction.setExtension(toExten);

			ServerCore.getCoreElement().addTransaction(callIdHeader.getCallId(), clientTransaction);
			if (logger.isTraceEnabled()) {
				logger.trace("Sending Request :" + request.toString());
			}
			clientTransaction.sendRequestMessage();
			return clientTransaction;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
