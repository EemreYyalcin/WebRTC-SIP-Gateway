package sipserver.com.executer.sip.transaction;

import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.invite.InviteClientTransaction;
import sipserver.com.executer.sip.invite.InviteServerTransaction;
import sipserver.com.executer.sip.options.OptionsClientTransaction;
import sipserver.com.executer.sip.options.OptionsServerTransaction;
import sipserver.com.executer.sip.register.RegisterClientTransaction;
import sipserver.com.executer.sip.register.RegisterServerTransaction;
import sipserver.com.parameter.constant.Constant.TransportType;

public class TransactionBuilder {

	private static Logger logger = Logger.getLogger(TransactionBuilder.class);

	public static ServerTransaction createServerTransaction(Request request, TransportType transportType, Session session) {
		try {
			if (Objects.isNull(request) || Objects.isNull(transportType)) {
				return null;
			}

			ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
			if (Objects.isNull(viaHeader)) {
				logger.error("Via Header is null!!");
				return null;
			}

			Extension extension = getExtension(request);
			if (Objects.isNull(extension)) {
				Response response = ServerCore.getCoreElement().getMessageFactory().createResponse(Response.FORBIDDEN, request);
				ServerCore.getServerCore().getTransport(transportType).sendSipMessage(response, viaHeader.getHost(), viaHeader.getPort(), session);
				return null;
			}

			extension.setAddress(viaHeader.getHost());
			extension.setTransportType(transportType);
			extension.setSession(session);

			ServerTransaction serverTransaction = null;
			if (request.getMethod().equals(Request.REGISTER)) {
				serverTransaction = new RegisterServerTransaction(extension);
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				serverTransaction = new OptionsServerTransaction(extension);
			} else if (request.getMethod().equals(Request.INVITE)) {
				serverTransaction = new InviteServerTransaction(extension);
			}
			if (Objects.isNull(serverTransaction)) {
				// logger.error("ServerTransaction Not Found Method:" + request.getMethod());
				return null;
			}
			serverTransaction.setRequest(request);
			ServerCore.getCoreElement().addTransaction(((CallIdHeader) request.getHeader(CallIdHeader.NAME)).getCallId(), serverTransaction);
			return serverTransaction;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ClientTransaction createClientTransaction(Request request, Extension toExten) {
		try {

			if (Objects.isNull(request) || Objects.isNull(toExten.getAddress()) || Objects.isNull(toExten.getTransportType())) {
				return null;
			}

			CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
			if (Objects.isNull(callIdHeader)) {
				return null;
			}

			ClientTransaction clientTransaction = (ClientTransaction) ServerCore.getCoreElement().findTransaction(callIdHeader.getCallId());
			if (Objects.nonNull(clientTransaction)) {
				clientTransaction.getTransport().sendSipMessage(clientTransaction.getRequest(), clientTransaction.getAddress(), clientTransaction.getPort(), clientTransaction.getSession());
				return clientTransaction;
			}

			if (request.getMethod().equals(Request.REGISTER)) {
				clientTransaction = new RegisterClientTransaction(toExten);
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				clientTransaction = new OptionsClientTransaction(toExten);
			} else if (request.getMethod().equals(Request.INVITE)) {
				clientTransaction = new InviteClientTransaction(toExten);
			}

			if (Objects.isNull(clientTransaction)) {
				logger.error("ClientTransaction Not Found Method:" + request.getMethod());
				return null;
			}

			clientTransaction.setRequest(request);
			ServerCore.getCoreElement().addTransaction(callIdHeader.getCallId(), clientTransaction);
			return clientTransaction;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Extension getExtension(Request request) {
		Extension extension = null;
		try {
			ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);
			if (Objects.isNull(contactHeader)) {
				return null;
			}
			extension = ExtensionBuilder.getExtension(contactHeader, (ViaHeader) request.getHeader(ViaHeader.NAME));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return extension;
	}

}
