package sipserver.com.service.invite;

import java.util.UUID;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.parameter.param.CallParam;
import sipserver.com.server.SipServerTransport;
import sipserver.com.server.auth.AccountManagerImpl;
import sipserver.com.service.Service;
import sipserver.com.service.util.CreateService;
import sipserver.com.util.log.LogTest;

public class InviteServiceOut extends Service {

	private static StackLogger logger = CommonLogger.getLogger(InviteServiceOut.class);

	public InviteServiceOut() {
		super(logger);
	}

	@Override
	public void processRequest(RequestEvent requestEvent, SipServerTransport transport) throws Exception {
		// NON
	}

	@Override
	public void processResponse(ResponseEvent responseEvent, SipServerTransport transport) {
		try {
			if (responseEvent.getClientTransaction() == null) {
				throw new Exception();
			}

			if (responseEvent.getClientTransaction().getRequest() == null) {
				throw new Exception();
			}

			ContactHeader contactHeader = (ContactHeader) responseEvent.getClientTransaction().getRequest().getHeader(ContactHeader.NAME);
			if (contactHeader == null) {
				throw new Exception();
			}

			FromHeader fromHeader = (FromHeader) responseEvent.getResponse().getHeader(FromHeader.NAME);
			if (fromHeader == null) {
				throw new Exception();
			}

			ViaHeader viaHeader = (ViaHeader) responseEvent.getResponse().getHeader(ViaHeader.NAME);
			if (viaHeader == null) {
				return;
			}

			if (viaHeader.getBranch() == null || viaHeader.getBranch().length() == 0) {
				return;
			}

			int statusCode = responseEvent.getResponse().getStatusCode();
			CallParam callParam = getChannel(viaHeader.getBranch());
			callParam.setResponseCode(statusCode).setRecievedResponse(true);
			callParam.setRecievedResponse(true);
			if (responseEvent.getResponse().getRawContent() != null) {
				callParam.setSdpRemoteContent(new String(responseEvent.getResponse().getRawContent()));
			}
			notifyWait(viaHeader.getBranch());
			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				if (!isHaveAuthenticateHeader(responseEvent)) {
					logger.logFatalError("Transaction is dead ");
					throw new Exception();
				}

				AuthenticationHelper authenticationHelper = ((SipStackExt) transport.getSipStack()).getAuthenticationHelper(new AccountManagerImpl(callParam.getExtension()), transport.getHeaderFactory());
				ClientTransaction clientTransaction = authenticationHelper.handleChallenge(responseEvent.getResponse(), responseEvent.getClientTransaction(), transport.getSipProvider(), 5, false);
				ServerCore.getServerCore().getTransportService().sendRequestMessage(clientTransaction);
				return;
			}
			if (statusCode == Response.FORBIDDEN) {
				logger.logFatalError("Forbidden " + callParam.getExtension().getExten());
				logger.logFatalError("Transaction is dead " + callParam.getExtension().getExten());
				takeChannel(viaHeader.getBranch());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			Request request = CreateService.createInviteMessage(toCallParam);
			SipServerTransport transport = ServerCore.getTransport(request);
			if (transport == null) {
				getLogger().logFatalError("Transport is null, ssasfddgs");
				throw new Exception();
			}
			request.setContent(toCallParam.getSdpLocalContent(), transport.getHeaderFactory().createContentTypeHeader("application", "sdp"));
			ClientTransaction toClientTransaction = transport.getSipProvider().getNewClientTransaction(request);
			toCallParam.setTransaction(toClientTransaction).setRequest(request);
			ServerCore.getServerCore().getTransportService().sendRequestMessage(toClientTransaction);
			String branch = ((ViaHeader) request.getHeader(ViaHeader.NAME)).getBranch();
			putChannel(branch, toCallParam);
			System.out.println("Sended InviteOut");
			waitResponse(branch, SipServerSharedProperties.messageTimeout);
			if (!toCallParam.isRecievedResponse()) {
				System.out.println("Recieved Timeout");
				ServerCore.getServerCore().getStatusService().busy(toCallParam);
				return;
			}

			if (toCallParam.getResponseCode() == SipServerSharedProperties.errorResponseCode) {
				LogTest.log("Call Error");
				ServerCore.getServerCore().getStatusService().error(toCallParam);
				throw new Exception();
			}

			int timeoutCount = SipServerSharedProperties.tryingTimeoutCount;
			while (toCallParam.getResponseCode() == Response.TRYING && timeoutCount > 0) {
				waitResponse(branch, SipServerSharedProperties.messageTimeout);
				--timeoutCount;
			}

			if (toCallParam.getResponseCode() == Response.TRYING) {
				System.out.println("Call Not Answered");
				ServerCore.getServerCore().getStatusService().noAnswer(toCallParam);
				beginCancelFlow(toCallParam, transport);
				return;
			}

			if (toCallParam.getResponseCode() == Response.UNAUTHORIZED) {
				toCallParam.setRecievedResponse(false);
				waitResponse(branch, SipServerSharedProperties.messageTimeout);
				if (!toCallParam.isRecievedResponse()) {
					System.out.println("Second Invite Timeout");
					ServerCore.getServerCore().getStatusService().busy(toCallParam);
					return;
				}

				if (toCallParam.getResponseCode() == Response.UNAUTHORIZED) {
					ServerCore.getServerCore().getStatusService().busy(toCallParam);
					return;
				}

			}
			if (toCallParam.getResponseCode() == Response.RINGING || toCallParam.getResponseCode() == Response.SESSION_PROGRESS) {
				System.out.println("Second Invite Timeout");
				ServerCore.getServerCore().getStatusService().ringing(toCallParam);
				toCallParam.setRecievedResponse(false);
				waitResponse(branch, SipServerSharedProperties.messageTimeout);
				if (!toCallParam.isRecievedResponse()) {
					System.out.println("Call Not Answered");
					ServerCore.getServerCore().getStatusService().noAnswer(toCallParam);
					beginCancelFlow(toCallParam, transport);
					return;
				}
			}

			if (toCallParam.getResponseCode() == Response.DECLINE) {
				LogTest.log("Declined");
				ServerCore.getServerCore().getStatusService().declined(toCallParam);
				return;
			}

			if (toCallParam.getResponseCode() == Response.BUSY_HERE) {
				LogTest.log("Busy Detected");
				ServerCore.getServerCore().getStatusService().busy(toCallParam);
				return;
			}

			if (toCallParam.getResponseCode() == Response.OK) {
				LogTest.log("OK Detected");
				ServerCore.getServerCore().getStatusService().ok(toCallParam, toCallParam.getSdpRemoteContent());
				return;
			}
			return;

		} catch (Exception e) {
			e.printStackTrace();
			ServerCore.getServerCore().getStatusService().error(toCallParam);
			return;
		}

	}

	private void waitResponse(String key, int messageTimeout) throws Exception {
		String lockValue = UUID.randomUUID().toString();
		lockProperties.put(key, lockValue);
		synchronized (lockValue) {
			lockValue.wait(messageTimeout);
		}
	}

	private void notifyWait(String branch) {
		if (lockProperties.get(branch) != null) {
			synchronized (lockProperties.get(branch)) {
				lockProperties.get(branch).notify();
			}
			lockProperties.remove(branch);
		}
	}

	public void beginCancelFlow(CallParam toCallParam, SipServerTransport transport) {
		try {

			Request cancelRequest = ((ClientTransaction) toCallParam.getTransaction()).createCancel();
			ClientTransaction cancelClientTransaction = transport.getSipProvider().getNewClientTransaction(cancelRequest);
			ServerCore.getServerCore().getTransportService().sendRequestMessage(cancelClientTransaction);
			waitResponse(cancelClientTransaction.getBranchId(), SipServerSharedProperties.messageTimeout);
			if (!toCallParam.isRecievedResponse()) {
				LogTest.log("Cancel Timeout");
				return;
			}

			if (toCallParam.getResponseCode() == SipServerSharedProperties.errorResponseCode) {
				LogTest.log("Cancell Error");
				throw new Exception();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
