package sipserver.com.service.options;

import java.util.ArrayList;
import java.util.UUID;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.CallID;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.Service;
import sipserver.com.service.util.CreateService;
import sipserver.com.service.util.GeneraterService;

public class OptionsServiceOut extends Service {

	private static StackLogger logger = CommonLogger.getLogger(OptionsServiceOut.class);

	public OptionsServiceOut() {
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

			ToHeader toHeader = (ToHeader) responseEvent.getResponse().getHeader(ToHeader.NAME);

			if (toHeader == null) {
				throw new Exception();
			}

			Extension trunkExtension = CreateService.createExtension(toHeader);
			if (trunkExtension == null) {
				throw new Exception();
			}
			int statusCode = responseEvent.getResponse().getStatusCode();

			ServerCore.getServerCore().getTrunkExtension(trunkExtension.getExten()).getExtensionParameter().setRegisterResponseRecieved(true);
			ServerCore.getServerCore().getTrunkExtension(trunkExtension.getExten()).getExtensionParameter().setOptionsResponseCode(statusCode);

			if (lockProperties.get(trunkExtension.getExten()) != null) {
				synchronized (lockProperties.get(trunkExtension.getExten())) {
					lockProperties.get(trunkExtension.getExten()).notify();
				}
				lockProperties.remove(trunkExtension.getExten());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void ping(Extension extTrunk) {
		try {
			Request requestMessage = createRegisterMessage(extTrunk);
			SipServerTransport transport = ServerCore.getTransport(requestMessage);
			if (transport == null) {
				getLogger().logFatalError("Transport is null, asfas");
				throw new Exception();
			}
			extTrunk.getExtensionParameter().setOptionsResponseRecieved(false);
			ServerCore.getServerCore().getTransportService().sendRequestMessage(transport.getSipProvider().getNewClientTransaction(requestMessage));
			String lockValue = UUID.randomUUID().toString();
			lockProperties.put(extTrunk.getExten(), lockValue);
			synchronized (lockValue) {
				lockValue.wait(SipServerSharedProperties.messageTimeout);
			}
			if (!extTrunk.getExtensionParameter().isOptionsResponseRecieved()) {
				extTrunk.setAlive(false);
				return;
			}

			if (extTrunk.getExtensionParameter().getOptionsResponseCode() == 911) {
				return;
			}

			extTrunk.setAlive(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Request createRegisterMessage(Extension extension) {
		try {
			SipServerTransport transport = ServerCore.getTransport(extension.getTransportType());
			if (transport == null) {
				throw new Exception();
			}

			SipURI fromAddress = transport.getAddressFactory().createSipURI("", transport.getHost());

			Address fromNameAddress = transport.getAddressFactory().createAddress(fromAddress);
			FromHeader fromHeader = transport.getHeaderFactory().createFromHeader(fromNameAddress, GeneraterService.getUUidForTag());
			// create To Header
			SipURI toAddress = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			Address toNameAddress = transport.getAddressFactory().createAddress(toAddress);
			if (extension.getDisplayName() != null) {
				toNameAddress.setDisplayName(extension.getDisplayName());
			}
			ToHeader toHeader = transport.getHeaderFactory().createToHeader(toNameAddress, null);

			// create Request URI
			String serverHostPort = ServerCore.getCoreElement().getLocalServerIp() + ":" + ServerCore.getCoreElement().getLocalSipPort();
			SipURI requestURI = transport.getAddressFactory().createSipURI(extension.getExten(), serverHostPort);

			// Create ViaHeaders

			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			ViaHeader viaHeader = transport.getHeaderFactory().createViaHeader(transport.getHost(), transport.getPort(), transport.getProtocol(), GeneraterService.getUUidForBranch());
			// add via headers
			viaHeaders.add(viaHeader);

			SipURI sipuri = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			sipuri.setPort(extension.getPort());
			sipuri.setLrParam();

			RouteHeader routeHeader = transport.getHeaderFactory().createRouteHeader(transport.getAddressFactory().createAddress(sipuri));

			CallIdHeader callIdHeader = new CallID(GeneraterService.getUUid(10));

			// Create a new Cseq header
			CSeqHeader cSeqHeader = transport.getHeaderFactory().createCSeqHeader(1L, Request.OPTIONS);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = transport.getHeaderFactory().createMaxForwardsHeader(70);

			// Create the request.
			Request request = transport.getMessageFactory().createRequest(requestURI, Request.OPTIONS, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			// Create contact headers

			SipURI contactUrl = transport.getAddressFactory().createSipURI(extension.getExten(), extension.getHost());
			contactUrl.setPort(transport.getPort());

			// Create the contact name address.
			SipURI contactURI = transport.getAddressFactory().createSipURI(extension.getExten(), transport.getHost());
			contactURI.setPort(transport.getPort());

			Address contactAddress = transport.getAddressFactory().createAddress(contactURI);

			if (extension.getDisplayName() != null) {
				// Add the contact address.
				contactAddress.setDisplayName(extension.getDisplayName());
			}

			ContactHeader contactHeader = transport.getHeaderFactory().createContactHeader(contactAddress);
			request.addHeader(contactHeader);
			// Dont use the Outbound Proxy. Use Lr instead.
			request.setHeader(routeHeader);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
