package sipserver.com.executer.sip.register;

import java.util.Objects;

import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.util.AliasService;

public class RegisterServerTransaction extends ServerTransaction {

	public RegisterServerTransaction(Extension extension) {
		super(extension);
	}

	@Override
	public void processRequest() {
		try {
			try {
				sendResponseMessage(Response.TRYING);

				if (getExtension().isRegister()) {
					getExtension().keepRegistered();
					info("Exten:" + getExtension().getExten() + " Keep Register.");
					sendResponseMessage(Response.OK);
					return;
				}

				if (Objects.isNull(getExtension().getPass())) {
					sendResponseMessage(Response.FORBIDDEN);
					info("Forbidden Peer Password has not setted Exten:" + getExtension().getExten());
					return;
				}

				if (!AliasService.isHaveAuthenticateHeader(getRequest())) {
					Response challengeResponse = ServerCore.getCoreElement().getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, getRequest());
					ServerCore.getCoreElement().getDigestServerAuthentication().generateChallenge(ServerCore.getCoreElement().getHeaderFactory(), challengeResponse, "nist.gov");
					ProxyAuthenticateHeader proxyAuthenticateHeader = (ProxyAuthenticateHeader) challengeResponse.getHeader(ProxyAuthenticateHeader.NAME);
					Objects.requireNonNull(proxyAuthenticateHeader);
					proxyAuthenticateHeader.setParameter("username", getExtension().getExten());
					sendResponseMessage(challengeResponse);
					return;
				}

				if (!ServerCore.getCoreElement().getDigestServerAuthentication().doAuthenticatePlainTextPassword(getRequest(), getExtension().getPass())) {
					sendResponseMessage(Response.FORBIDDEN);
					info("Forbidden Peer Wrong Password Exten:" + getExtension().getExten());
					return;
				}
				getExtension().keepRegistered();
				sendResponseMessage(Response.OK);
				info("Peer Registerd Exten:" + getExtension().getExten());

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

	@Override
	public void processACK() {
		super.processACK();
	}
}
