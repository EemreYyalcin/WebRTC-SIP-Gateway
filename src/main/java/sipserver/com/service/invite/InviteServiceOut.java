package sipserver.com.service.invite;

import javax.sip.ClientTransaction;
import javax.sip.DialogTerminatedEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.server.auth.AccountManagerImpl;
import sipserver.com.service.Service;
import sipserver.com.service.util.ExceptionService;
import sipserver.com.service.util.message.CreateMessageService;
import sipserver.com.util.log.LogTest;

public class InviteServiceOut extends Service {

	private static StackLogger logger = CommonLogger.getLogger(InviteServiceOut.class);

	public InviteServiceOut() {
		super(logger);
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport, ServerTransaction serverTransaction) throws Exception {
		// NON
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		CallParam toCallParam = null;
		try {

			ToHeader toHeader = (ToHeader) responseEvent.getResponse().getHeader(ToHeader.NAME);
			ExceptionService.checkNullObject(toHeader);

			Extension toExtension = CreateMessageService.createExtension(toHeader);
			if (toExtension == null) {
				LogTest.log(this, "Response Fail ToHeader!!");
				return;
			}

			FromHeader fromHeader = (FromHeader) responseEvent.getResponse().getHeader(FromHeader.NAME);
			if (fromHeader == null) {
				throw new Exception();
			}

			toCallParam = ServerCore.getServerCore().getChannelControlService().getChannel(toExtension.getExten(), responseEvent.getClientTransaction());
			if (toCallParam == null) {
				LogTest.log(this, "Channel Not Found");
				return;
			}
			int statusCode = responseEvent.getResponse().getStatusCode();
			if (responseEvent.getResponse().getRawContent() != null) {
				toCallParam.setSdpRemoteContent(new String(responseEvent.getResponse().getRawContent()));
			}

			if (statusCode == Response.RINGING || statusCode == Response.SESSION_PROGRESS) {
				ServerCore.getServerCore().getBridgeService().ringing(toCallParam);
				return;
			}

			if (statusCode == Response.DECLINE) {
				LogTest.log("Declined");
				ServerCore.getServerCore().getBridgeService().declined(toCallParam);
				ServerCore.getServerCore().getChannelControlService().takeChannel(toExtension.getExten(), responseEvent.getClientTransaction());
				return;
			}
			if (statusCode == Response.FORBIDDEN) {
				logger.logFatalError("Forbidden " + toCallParam.getExtension().getExten());
				logger.logFatalError("Transaction is dead " + toCallParam.getExtension().getExten());
				ServerCore.getServerCore().getChannelControlService().takeChannel(toExtension.getExten(), responseEvent.getClientTransaction());
				return;
			}

			if (statusCode == Response.BUSY_HERE) {
				LogTest.log("Busy Detected");
				ServerCore.getServerCore().getBridgeService().busy(toCallParam);
				ServerCore.getServerCore().getChannelControlService().takeChannel(toExtension.getExten(), responseEvent.getClientTransaction());
				return;
			}
			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				if (!isHaveAuthenticateHeader(responseEvent)) {
					logger.logFatalError("Transaction is dead ");
					throw new Exception();
				}

				AuthenticationHelper authenticationHelper = ((SipStackExt) transport.getSipStack()).getAuthenticationHelper(new AccountManagerImpl(toCallParam.getExtension()), transport.getHeaderFactory());
				ClientTransaction clientTransaction = authenticationHelper.handleChallenge(responseEvent.getResponse(), (ClientTransaction) toCallParam.getTransaction(), transport.getSipProvider(), 5, false);
				ServerCore.getServerCore().getTransportService().sendRequestMessage(clientTransaction);
				ServerCore.getServerCore().getChannelControlService().takeChannel(toExtension.getExten(), responseEvent.getClientTransaction());
				ServerCore.getServerCore().getChannelControlService().putChannel(toExtension.getExten(), toCallParam);
				return;
			}

			if (statusCode == Response.OK) {
				LogTest.log("OK Detected");
				ServerCore.getServerCore().getBridgeService().ok(toCallParam);
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
			LogTest.log("Call Error");
			if (toCallParam != null) {
				ServerCore.getServerCore().getBridgeService().error(toCallParam);
				ServerCore.getServerCore().getChannelControlService().takeChannel(toCallParam.getExtension().getExten(), responseEvent.getClientTransaction());
			}
		}

	}

	// private String getSdp() {
	// return "v=0\r\n" + "o=1002 559 930 IN IP4 192.168.1.106\r\n" +
	// "s=mizudroid\r\n" + "c=IN IP4 192.168.1.106\r\n" + "t=0 0\r\n" + "m=audio
	// 17970 RTP/AVP 105 0 97 3 101\r\n" + "a=rtpmap:105 speex/16000\r\n" +
	// "a=fmtp:105 mode=8;mode=any\r\n" + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:97
	// iLBC/8000\r\n" + "a=fmtp:97 mode=30\r\n" + "a=rtpmap:101
	// telephone-event/8000\r\n" + "a=fmtp:101 0-16\r\n" + "a=sendrecv";
	//
	// }

	public void beginCall(CallParam toCallParam) {
		try {
			Request request = CreateMessageService.createInviteMessage(toCallParam);
			SipServerTransport transport = ServerCore.getTransport(request);
			if (transport == null) {
				getLogger().logFatalError("Transport is null, ssasfddgs");
				throw new Exception();
			}
			request.setContent(toCallParam.getSdpLocalContent(), transport.getHeaderFactory().createContentTypeHeader("application", "sdp"));
			ClientTransaction toClientTransaction = transport.getSipProvider().getNewClientTransaction(request);
			toCallParam.setTransaction(toClientTransaction).setRequest(request);
			ServerCore.getServerCore().getChannelControlService().putChannel(toCallParam.getExtension().getExten(), toCallParam);
			ServerCore.getServerCore().getTransportService().sendRequestMessage((ClientTransaction) toCallParam.getTransaction());
		} catch (Exception e) {
			e.printStackTrace();
			ServerCore.getServerCore().getBridgeService().error(toCallParam);
			return;
		}

	}

	public void beginCancelFlow(CallParam toCallParam, SipServerTransport transport) {
		try {

			Request cancelRequest = ((ClientTransaction) toCallParam.getTransaction()).createCancel();
			ClientTransaction cancelClientTransaction = transport.getSipProvider().getNewClientTransaction(cancelRequest);
			ServerCore.getServerCore().getTransportService().sendRequestMessage(cancelClientTransaction);
			LogTest.log(this, "Cancel Message Sended");
			// TODO: Waitng Cancel Response
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processTimeout(TimeoutEvent timeoutEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent terminatedEvent) {
		// TODO Auto-generated method stub

	}

}
