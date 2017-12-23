package sipserver.com.core.sip.service;

import java.util.Objects;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import sipserver.com.core.sip.builder.MessageBuilder;
import sipserver.com.core.sip.handler.invite.InviteClientMessageHandler;
import sipserver.com.core.sip.parameter.param.CallParam;
import sipserver.com.executer.starter.ServerCore;
import sipserver.com.executer.starter.SipServerSharedProperties;
import sipserver.com.util.operation.MicroOperation;

public class RouteService {

	private static Logger logger = Logger.getLogger(RouteService.class);

	public static boolean route(CallParam fromCallParam) {
		try {

			if (Objects.isNull(fromCallParam)) {
				logger.error("Please Control Route Parameter!");
				return false;
			}
			if (MicroOperation.isAnyNull(fromCallParam.getMessageHandler(), fromCallParam.getSdpLocalContent())) {
				logger.error("Please Control Route Parameter SDP Or Handler!");
				return false;
			}
			CallParam toCallParam = fromCallParam.getMessageHandler().getToCallParam();
			if (Objects.isNull(toCallParam)) {
				logger.error("Please Control Route Parameter Incoming Request!");
				return false;
			}

			if (!toCallParam.getExtension().isRegister()) {
				fromCallParam.getMessageHandler().onReject(Response.BUSY_HERE);
				logger.debug("Not Route 1");
				return false;
			}

			if (!toCallParam.getExtension().isAlive()) {
				fromCallParam.getMessageHandler().onReject(Response.BUSY_HERE);
				logger.debug("Not Route 2 " + toCallParam.getExtension().getExten());
				return false;
			}

			if (SipServerSharedProperties.mediaServerActive) {
				// OutgoingCallMediaSession outgoingCallMediaSession = new
				// OutgoingCallMediaSession(toCallParam, serverTransaction);
				//
				// MgcpSession mgcpSession = new MgcpSession(outgoingCallMediaSession);
				// if (Objects.isNull(mgcpSession)) {
				// return;
				// }
				// if (NullUtil.isNull(serverTransaction)) {
				// // TODO: Only Out Call Without Bridge
				// return;
				// }
				// toCallParam.setMgcpSession(mgcpSession);
				// mgcpSession.createBRIDGEwithEndpointName(serverTransaction.getCallParam().getMgcpSession().getSpecificEndpointName());
				logger.error("Not Implemented MediaServer ...");
				return false;
			}

			Request request = MessageBuilder.createInviteMessage(toCallParam, fromCallParam);
			if (Objects.isNull(request)) {
				fromCallParam.getMessageHandler().onReject(Response.BUSY_HERE);
				logger.debug("Not Route 3 " + toCallParam.getExtension().getExten());
				return false;
			}
			request.setContent(toCallParam.getSdpLocalContent(), ServerCore.getCoreElement().getHeaderFactory().createContentTypeHeader("application", "sdp"));

			InviteClientMessageHandler inviteClientMessageHandler = new InviteClientMessageHandler(request, toCallParam.getExtension());
			inviteClientMessageHandler.setCallParam(toCallParam);
			toCallParam.setMessageHandler(inviteClientMessageHandler);

			ServerCore.getCoreElement().addHandler(((CallIdHeader) request.getHeader(CallIdHeader.NAME)).getCallId(), inviteClientMessageHandler);
			ServerCore.getCoreElement().addBridgeElement(fromCallParam, toCallParam);

			inviteClientMessageHandler.onTrying();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void observeBridgingForReject(CallParam callParam, int statusCode) {
		CallParam toCallParam = ServerCore.getCoreElement().getBridgeElement(callParam);
		if (Objects.isNull(toCallParam)) {
			return;
		}
		toCallParam.getMessageHandler().onReject(statusCode);
	}

	public static void observeBridgingForBye(CallParam callParam) {
		CallParam toCallParam = ServerCore.getCoreElement().getBridgeElement(callParam);
		if (Objects.isNull(toCallParam)) {
			return;
		}
		toCallParam.getMessageHandler().onBye();
	}

	public static void observeBridgingForAcceptCall(CallParam callParam, String content) {
		CallParam toCallParam = ServerCore.getCoreElement().getBridgeElement(callParam);
		if (Objects.isNull(toCallParam)) {
			return;
		}
		toCallParam.getMessageHandler().onOk(content);
	}

	public static void observeBridgingForRinging(CallParam callParam) {
		CallParam toCallParam = ServerCore.getCoreElement().getBridgeElement(callParam);
		if (Objects.isNull(toCallParam)) {
			return;
		}
		toCallParam.getMessageHandler().onRinging();
	}

}
