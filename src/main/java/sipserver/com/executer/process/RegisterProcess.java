package sipserver.com.executer.process;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.message.Response;
import sipserver.com.server.SipServer;

public class RegisterProcess extends ProcessMessage {

	public RegisterProcess(SipServer sipServer) throws PeerUnavailableException {
		super(sipServer);
	}

	@Override
	public void processRequest(RequestEvent requestEvent) {
		try {
			System.out.println("RegisterRequestProcess:\r\n" + requestEvent.getRequest().toString());
			Response response = getMessageFactory().createResponse(Response.TRYING, requestEvent.getRequest());
			ServerTransaction serverTransaction = getSipServer().getProvider().getNewServerTransaction(requestEvent.getRequest());
			serverTransaction.sendResponse(response);

			DigestServerAuthenticationHelper dsam = new DigestServerAuthenticationHelper();
			if (!dsam.doAuthenticatePlainTextPassword(requestEvent.getRequest(), "test1001")) {
				Response challengeResponse = getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, requestEvent.getRequest());
				dsam.generateChallenge(getHeaderFactory(), challengeResponse, "nist.gov");
				serverTransaction.sendResponse(challengeResponse);
				return;
			}

			response = getMessageFactory().createResponse(200, requestEvent.getRequest());
			serverTransaction.sendResponse(response);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void processResponse(ResponseEvent responseEvent) {
		System.out.println("RegisterResponseProcess:\r\n" + responseEvent.getResponse().toString());
	}

}
