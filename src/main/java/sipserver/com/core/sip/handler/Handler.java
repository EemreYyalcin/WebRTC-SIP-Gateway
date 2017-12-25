package sipserver.com.core.sip.handler;

import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import gov.nist.javax.sip.message.SIPMessage;
import sipserver.com.core.sip.builder.HeaderBuilder;
import sipserver.com.core.sip.handler.invite.InviteServerMessageHandler;
import sipserver.com.core.sip.handler.options.OptionsServerMessageHandler;
import sipserver.com.core.sip.handler.register.RegisterServerMessageHandler;
import sipserver.com.executer.starter.ServerCore;
import sipserver.com.util.operation.MicroOperation;

public class Handler {

	private static Logger logger = Logger.getLogger(Handler.class);

	public static void processSipMessage(SIPMessage message) {
		processSipMessage(message, null);
	}

	public static void processSipMessage(SIPMessage message, Session session) {
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

		String callId = callIdHeader.getCallId();
		if (Objects.isNull(callId)) {
			logger.warn("Sip Message has not CALLID 2. ");
			logger.warn(message.toString());
			return;
		}

		MessageHandler handler = ServerCore.getCoreElement().findHandler(callId);
		if (message instanceof Response) {
			processResponse((Response) message, handler, session);
			return;
		}
		processRequest((Request) message, handler, session);
	}

	private static void processRequest(Request request, MessageHandler handler, Session session) {
		try {

			if (Objects.nonNull(handler)) {
				if (request.getMethod().equals(Request.BYE)) {
					handler.onBye(request);
					return;
				}
				if (request.getMethod().equals(Request.CANCEL)) {
					handler.onCancel(request);
					return;
				}
				if (request.getMethod().equals(Request.ACK)) {
					handler.onACK();
					return;
				}
				logger.error("Specific Request Message");
				return;
			}
			String remoteAddress = null;
			int remotePort = 0;
			if (Objects.isNull(session)) {
				ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
				if (Objects.isNull(viaHeader)) {
					logger.error("Via Header is null!!");
					return;
				}
				remoteAddress = viaHeader.getHost();
				remotePort = viaHeader.getPort();
			}

			MessageHandler messageHandler = null;
			if (request.getMethod().equals(Request.REGISTER)) {
				messageHandler = (session == null) ? new RegisterServerMessageHandler(request, remoteAddress, remotePort) : new RegisterServerMessageHandler(request, session);
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				messageHandler = (session == null) ? new OptionsServerMessageHandler(request, remoteAddress, remotePort) : new OptionsServerMessageHandler(request, session);
			} else if (request.getMethod().equals(Request.INVITE)) {
				messageHandler = (session == null) ? new InviteServerMessageHandler(request, remoteAddress, remotePort) : new InviteServerMessageHandler(request, session);
			}

			if (Objects.isNull(messageHandler)) {
				logger.trace("Ignored Message " + request.getMethod());
				return;
			}

			ServerCore.getCoreElement().addHandler(((CallIdHeader) request.getHeader(CallIdHeader.NAME)).getCallId(), messageHandler);
			messageHandler.onTrying();

		} catch (Exception e) {
			logger.error("Error Request Message !! Message: " + request.toString());
			e.printStackTrace();
		}
	}

	private static void processResponse(Response response, MessageHandler handler, Session session) {
		try {
			if (Objects.isNull(handler)) {
				logger.warn("ClientTransaction has not Found " + response.toString());
				return;
			}
			handler.setResponse(response);

			int statusCode = response.getStatusCode();
			if (MicroOperation.isAnyNotNull(handler.getCallParam(), response.getRawContent())) {
				handler.getCallParam().setSdpRemoteContent(new String(response.getRawContent()));
			}

			if (response.getStatusCode() == Response.RINGING || statusCode == Response.SESSION_PROGRESS) {
				handler.onRinging();
				return;
			}

			if (statusCode == Response.DECLINE) {
				logger.info("Declined From Exten:" + response.getHeader(FromHeader.NAME));
				handler.onReject(response.getStatusCode());
				return;
			}
			if (statusCode == Response.FORBIDDEN) {
				logger.warn("Forbidden From Exten:" + response.getHeader(FromHeader.NAME));
				handler.onReject(response.getStatusCode());
				return;
			}

			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				if (!HeaderBuilder.isHaveAuthenticateHeader(response)) {
					logger.error("Response has not Authenticate Header");
					return;
				}

				// AuthenticationHelper authenticationHelper = ((SipStackExt)
				// transport.getSipStack()).getAuthenticationHelper(new
				// AccountManagerImpl(toCallParam.getExtension()),
				// transport.getHeaderFactory());
				// ClientTransaction clientTransaction =
				// authenticationHelper.handleChallenge(response, (ClientTransaction)
				// toCallParam.getTransaction(), transport.getSipProvider(), 5, false);
				// TransportService().sendRequestMessage(clientTransaction);
				// ChannelControlService().takeChannel(toExtension.getExten(),
				// responseEvent.getClientTransaction());
				// ChannelControlService().putChannel(toExtension.getExten(),
				// toCallParam);
				return;
			}

			if (response.getStatusCode() == Response.BUSY_HERE) {
				logger.warn("Busy Detected From Exten:" + response.getHeader(FromHeader.NAME));
				handler.onReject(response.getStatusCode());
				return;
			}

			if (response.getStatusCode() == Response.OK) {
				handler.onOk();
				return;
			}

			if (response.getStatusCode() < 300 && response.getStatusCode() > 200) {
				logger.warn("Specific Response Code: " + response.getStatusCode());
				handler.onOk();
				return;
			}

			if (response.getStatusCode() > 300) {
				logger.warn("Specific Response Code2: " + response.getStatusCode());
				handler.onReject(response.getStatusCode());
				return;
			}
			logger.error("Specific Response Message");
		} catch (Exception e) {
			logger.error("Error Resoonse Message " + response.toString());
			e.printStackTrace();
		}

	}

}
