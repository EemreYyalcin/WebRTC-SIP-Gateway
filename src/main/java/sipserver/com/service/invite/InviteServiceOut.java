package sipserver.com.service.invite;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.header.CallID;

import java.util.ArrayList;
import java.util.UUID;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.server.SipServerTransport;
import sipserver.com.server.auth.AccountManagerImpl;
import sipserver.com.service.Service;
import sipserver.com.service.param.ChannelParameter;
import sipserver.com.service.util.GeneraterService;

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

			String callId = ((CallIdHeader) responseEvent.getResponse().getHeader(CallIdHeader.NAME)).getCallId();
			ChannelParameter channel = getChannel(callId);

			Extension trunkExtension = new Extension(fromHeader);
			int statusCode = responseEvent.getResponse().getStatusCode();
			channel.setToResponseCode(statusCode);
			channel.setToIsRecievedResponse(true);
			if (responseEvent.getResponse().getRawContent() != null) {
				getChannel(callId).setToSdpContent(new String(responseEvent.getResponse().getRawContent()));
			}
			if (lockProperties.get(callId) != null) {
				synchronized (lockProperties.get(callId)) {
					lockProperties.get(callId).notify();
				}
			}
			if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
				if (!isHaveAuthenticateHeader(responseEvent)) {
					logger.logFatalError("Transaction is dead ");
					throw new Exception();
				}
				AuthenticationHelper authenticationHelper = ((SipStackExt) transport.getSipStack()).getAuthenticationHelper(new AccountManagerImpl(trunkExtension), transport.getHeaderFactory());
				ClientTransaction clientTransaction = authenticationHelper.handleChallenge(responseEvent.getResponse(), responseEvent.getClientTransaction(), transport.getSipProvider(), 5, false);
				ServerCore.getServerCore().getTransportService().sendRequestMessage(clientTransaction);
				return;
			}
			if (statusCode == Response.FORBIDDEN) {
				logger.logFatalError("Forbidden " + trunkExtension.getExten());
				logger.logFatalError("Transaction is dead " + trunkExtension.getExten());
				takeChannel(callId);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int beginCall(ChannelParameter channelParameter) {
		try {
			if (channelParameter.getFromExtension() == null || channelParameter.getToExtension() == null) {
				System.out.println("ChannelParameter Error");
				return 500;
			}
			if (channelParameter.getFromTransaction() == null) {
				System.out.println("ChannelParameter Error Transaction Error!");
				return 500;
			}
			// TODO: CreateConnection Mgcp Command
			String sdpData = getSdp();
			Request request = createInviteMessage(channelParameter);
			SipServerTransport transport = ServerCore.getTransport(request);
			if (transport == null) {
				getLogger().logFatalError("Transport is null, ssasfddgs");
				throw new Exception();
			}
			request.setContent(sdpData, transport.getHeaderFactory().createContentTypeHeader("application", "sdp"));
			ClientTransaction toClientTransaction = transport.getSipProvider().getNewClientTransaction(request);
			channelParameter.setToTransaction(toClientTransaction);
			ServerCore.getServerCore().getTransportService().sendRequestMessage(toClientTransaction);
			String callId = ((CallIdHeader) request.getHeader(CallIdHeader.NAME)).getCallId();
			putChannel(callId, channelParameter);
			System.out.println("Sended InviteOut");
			String lockValue = UUID.randomUUID().toString();
			lockProperties.put(callId, lockValue);
			synchronized (lockValue) {
				lockValue.wait(SipServerSharedProperties.messageTimeout);
			}
			if (!channelParameter.isToIsRecievedResponse()) {
				System.out.println();
				return 604;
			}

			if (channelParameter.getToResponseCode() != SipServerSharedProperties.errorResponseCode && channelParameter.getToResponseCode() == Response.UNAUTHORIZED) {
				channelParameter.setToIsRecievedResponse(false);
				lockValue = UUID.randomUUID().toString();
				lockProperties.put(callId, lockValue);
				synchronized (lockValue) {
					lockValue.wait(SipServerSharedProperties.messageTimeout);
				}
				if (!channelParameter.isToIsRecievedResponse()) {
					System.out.println("Second Invite Timeout");
					return 604;
				}

				if (channelParameter.getToResponseCode() != 200) {
					System.out.println("Second Invite Timeout");
					return channelParameter.getToResponseCode();
				}

			}

			return 200;

		} catch (Exception e) {
			e.printStackTrace();
			return 500;
		}

	}

	private String getSdp() {
		return "v=0\r\n" + "o=1002 559 930 IN IP4 192.168.1.106\r\n" + "s=mizudroid\r\n" + "c=IN IP4 192.168.1.106\r\n" + "t=0 0\r\n" + "m=audio 17970 RTP/AVP 105 0 97 3 101\r\n" + "a=rtpmap:105 speex/16000\r\n" + "a=fmtp:105 mode=8;mode=any\r\n" + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:97 iLBC/8000\r\n" + "a=fmtp:97 mode=30\r\n" + "a=rtpmap:101 telephone-event/8000\r\n" + "a=fmtp:101 0-16\r\n" + "a=sendrecv";

	}

	public Request createInviteMessage(ChannelParameter channelParameter) {
		try {

			SipServerTransport transport = ServerCore.getTransport(channelParameter.getToExtension().getTransportType());
			if (transport == null) {
				throw new Exception();
			}

			SipURI fromAddress = transport.getAddressFactory().createSipURI(channelParameter.getFromExtension().getExten(), channelParameter.getFromExtension().getHost());

			Address fromNameAddress = transport.getAddressFactory().createAddress(fromAddress);
			if (channelParameter.getFromExtension().getDisplayName() != null) {
				fromNameAddress.setDisplayName(channelParameter.getFromExtension().getDisplayName());
			}

			FromHeader fromHeader = transport.getHeaderFactory().createFromHeader(fromNameAddress, GeneraterService.getUUidForTag());
			// create To Header
			SipURI toAddress = transport.getAddressFactory().createSipURI(channelParameter.getToExtension().getExten(), channelParameter.getToExtension().getHost());
			Address toNameAddress = transport.getAddressFactory().createAddress(toAddress);
			if (channelParameter.getToExtension().getDisplayName() != null) {
				toNameAddress.setDisplayName(channelParameter.getToExtension().getDisplayName());
			}
			ToHeader toHeader = transport.getHeaderFactory().createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI = transport.getAddressFactory().createSipURI(channelParameter.getToExtension().getExten(), transport.getHost() + ":" + transport.getPort());

			// Create ViaHeaders

			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			ViaHeader viaHeader = transport.getHeaderFactory().createViaHeader(transport.getHost(), transport.getPort(), transport.getProtocol(), GeneraterService.getUUidForBranch());
			// add via headers
			viaHeaders.add(viaHeader);

			// SipURI sipuri =
			// transport.getAddressFactory().createSipURI(extension.getExten(),
			// extension.getHost());
			// sipuri.setPort(extension.getPort());
			// sipuri.setLrParam();
			//
			// RouteHeader routeHeader =
			// transport.getHeaderFactory().createRouteHeader(transport.getAddressFactory().createAddress(sipuri));

			// Create a new CallId header
			// CallIdHeader callIdHeader = sipProvider.getNewCallId();
			CallIdHeader callIdHeader = new CallID(GeneraterService.getUUid(10));

			// Create a new Cseq header
			CSeqHeader cSeqHeader = transport.getHeaderFactory().createCSeqHeader(1L, Request.INVITE);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = transport.getHeaderFactory().createMaxForwardsHeader(70);

			// Create the request.
			Request request = transport.getMessageFactory().createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			// Create contact headers
			SipURI contactUrl = transport.getAddressFactory().createSipURI(channelParameter.getFromExtension().getExten(), transport.getHost());
			contactUrl.setPort(transport.getPort());

			// Create the contact name address.
			SipURI contactURI = transport.getAddressFactory().createSipURI(channelParameter.getFromExtension().getExten(), transport.getHost());
			contactURI.setPort(transport.getPort());

			Address contactAddress = transport.getAddressFactory().createAddress(contactURI);

			if (channelParameter.getFromExtension().getDisplayName() != null) {
				// Add the contact address.
				contactAddress.setDisplayName(channelParameter.getFromExtension().getDisplayName());
			}

			ContactHeader contactHeader = transport.getHeaderFactory().createContactHeader(contactAddress);
			request.addHeader(contactHeader);
			// TODO: Dont use the Outbound Proxy. Use Lr instead. RouteHeader
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
