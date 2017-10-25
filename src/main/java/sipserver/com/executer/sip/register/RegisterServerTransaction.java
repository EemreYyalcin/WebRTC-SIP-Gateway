package sipserver.com.executer.sip.register;

import java.net.InetAddress;
import java.util.Objects;

import javax.sip.header.ContactHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.server.SipServerTransport;
import sipserver.com.service.util.AliasService;

public class RegisterServerTransaction extends ServerTransaction {

	public RegisterServerTransaction(Request request, InetAddress address, int port, SipServerTransport transport) {
		super(request, address, port, transport);
	}

	@Override
	public void processRequest() {
		try {
			try {
				sendResponseMessage(Response.TRYING);
				ContactHeader contactHeader = (ContactHeader) getRequest().getHeader(ContactHeader.NAME);
				if (Objects.isNull(contactHeader)) {
					sendResponseMessage(Response.UNAUTHORIZED);
					debug("UNAUTHORIZED Message Contact Header is undefined ");
					debug("address:" + getAddress() + "Register Undefined");
					return;
				}

				Extension extIncoming = Extension.getExtension(contactHeader);
				if (Objects.isNull(extIncoming)) {
					sendResponseMessage(Response.BAD_REQUEST);
					info("Peer is not Defined to Server");
					debug("address:" + getAddress() + "Register Peer Undefined");
					return;
				}
				extIncoming.setTransport(getTransport());

				ViaHeader viaHeader = (ViaHeader) getRequest().getHeader(ViaHeader.NAME);
				if (Objects.isNull(viaHeader)) {
					sendResponseMessage(Response.BAD_REQUEST);
					error("Via Header Unspecified ");
					if (!getLogger().isTraceEnabled()) {
						error("Via Header Error Message is \n" + getRequest().toString());
					}
					debug("address:" + getAddress() + "Register Peer Undefined");
					return;
				}

				if (extIncoming.isRegister()) {
					if (!extIncoming.getAddress().equals(extIncoming.getAddress())) {
						extIncoming.unregister();
						warn("UNAUTHORIZED Message Address not Match Unregister Peer Exten:" + extIncoming.getExten());
						sendResponseMessage(Response.UNAUTHORIZED);
						return;
					}
					extIncoming.keepRegistered();
					info("Exten:" + extIncoming.getExten() + " Keep Register.");
					sendResponseMessage(Response.OK);
					return;
				}
				if (!AliasService.isHaveAuthenticateHeader(getRequest())) {
					Response challengeResponse = getTransport().getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, getRequest());
					getTransport().getDigestServerAuthentication().generateChallenge(getTransport().getHeaderFactory(), challengeResponse, "nist.gov");
					ProxyAuthenticateHeader proxyAuthenticateHeader = (ProxyAuthenticateHeader) challengeResponse.getHeader(ProxyAuthenticateHeader.NAME);
					Objects.requireNonNull(proxyAuthenticateHeader);
					proxyAuthenticateHeader.setParameter("username", extIncoming.getExten());
					sendResponseMessage(challengeResponse);
					return;
				}

				if (Objects.isNull(extIncoming.getPass())) {
					sendResponseMessage(Response.FORBIDDEN);
					info("Forbidden Peer Password has not setted Exten:" + extIncoming.getExten());
					return;
				}

				if (!extIncoming.getTransport().getDigestServerAuthentication().doAuthenticatePlainTextPassword(getRequest(), extIncoming.getPass())) {
					sendResponseMessage(Response.FORBIDDEN);
					info("Forbidden Peer Wrong Password Exten:" + extIncoming.getExten());
					return;
				}
				extIncoming.keepRegistered();
				sendResponseMessage(Response.OK);
				info("Peer Registerd Exten:" + extIncoming.getExten());

			} catch (Exception e) {
				sendResponseMessage(Response.UNAUTHORIZED);
				e.printStackTrace();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			sendResponseMessage(Response.BAD_EVENT);
		}
	}

}
