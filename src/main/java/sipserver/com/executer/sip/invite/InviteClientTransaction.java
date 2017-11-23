package sipserver.com.executer.sip.invite;

import java.util.Objects;

import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.operational.BridgeService;
import sipserver.com.service.util.AliasService;

public class InviteClientTransaction extends ClientTransaction {

	@Override
	public void processResponse(Response response) {
		CallParam toCallParam = null;
		try {
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			Objects.requireNonNull(toHeader);

			Extension toExtension = ExtensionBuilder.getExtension(toHeader);
			if (Objects.isNull(toExtension)) {
				info("To Extension Not Found toHeader:" + toHeader.toString());
				return;
			}
			FromHeader fromHeader = (FromHeader) response.getHeader(FromHeader.NAME);
			Objects.requireNonNull(fromHeader);

			toCallParam = getCallParam();
			if (Objects.isNull(toCallParam)) {
				error("Channel is not Found from:" + fromHeader.toString() + "\n toExten:" + toExtension.getExten());
				return;
			}
			int statusCode = response.getStatusCode();
			if (Objects.nonNull(response.getRawContent())) {
				toCallParam.setSdpRemoteContent(new String(response.getRawContent()));
			}

			if (statusCode == Response.RINGING || statusCode == Response.SESSION_PROGRESS) {
				BridgeService.ringing(this);
				return;
			}

			if (statusCode == Response.DECLINE) {
				info("Declined From Exten:" + toExtension.getExten());
				BridgeService.declined(this);
				return;
			}
			if (statusCode == Response.FORBIDDEN) {
				warn("Forbidden From Exten:" + toExtension.getExten());
				return;
			}

			if (statusCode == Response.BUSY_HERE) {
				warn("Busy Detected From Exten:" + toExtension.getExten());
				BridgeService.busy(this);
				return;
			}
			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				if (!AliasService.isHaveAuthenticateHeader(response)) {
					error("Response has not Authenticate Header");
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

			if (statusCode == Response.OK) {
				info("Call Started Exten:" + toExtension.getExten());
				if (SipServerSharedProperties.mediaServerActive) {
					toCallParam.getMgcpSession().modify(new String(response.getRawContent()));
				} else {
					toCallParam.setSdpRemoteContent(new String(response.getRawContent()));
					getBridgeTransaction().getCallParam().setSdpLocalContent(toCallParam.getSdpRemoteContent());
					BridgeService.ok(this, toCallParam.getSdpRemoteContent());
				}
				setResponse(response);
				sendACK();
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
			error("Call Transction Error. Exception " + e.getMessage());
			if (toCallParam != null) {
				BridgeService.error(this);
			}
		}
	}

	public void beginCancelFlow(CallParam toCallParam, SipServerTransport transport) {
		try {

			// Request cancelRequest = ((ClientTransaction)
			// toCallParam.getTransaction()).createCancel();
			// ClientTransaction cancelClientTransaction =
			// transport.getSipProvider().getNewClientTransaction(cancelRequest);
			// LogTest.log(this, "Cancel Message Sended");
			// TODO: Waitng Cancel Response
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
