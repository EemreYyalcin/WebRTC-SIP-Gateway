package sipserver.com.service;

import java.util.EventObject;
import java.util.Properties;

import javax.sip.DialogTerminatedEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.WWWAuthenticate;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import sipserver.com.domain.Extension;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.core.SipServerSharedProperties;
import sipserver.com.server.SipServerTransport;
import sipserver.com.util.log.LogTest;

public abstract class Service {

	private StackLogger logger;
	protected Properties lockProperties = new Properties();

	public Service(StackLogger logger) {
		setLogger(logger);
	}

	public abstract void processRequest(RequestEvent requestEvent, SipServerTransport transport, ServerTransaction serverTransaction) throws Exception;

	public abstract void processResponse(ResponseEvent responseEvent, SipServerTransport transport);

	public abstract void processDialogTerminated(DialogTerminatedEvent event);

	public abstract void processTimeout(TimeoutEvent timeoutEvent);

	public abstract void processTransactionTerminated(TransactionTerminatedEvent terminatedEvent);

	public ServerTransaction getServerTransaction(SipProvider sipProvider, Request request) {
		try {
			return sipProvider.getNewServerTransaction(request);
		} catch (Exception exception) {
			LogTest.log("Transaction Already Exist " + request.getRequestURI());
			return null;
		}
	}

	public boolean isHaveAuthenticateHeader(EventObject event) {
		Message message = null;
		if (event instanceof RequestEvent) {
			message = ((RequestEvent) event).getRequest();
		} else {
			message = ((ResponseEvent) event).getResponse();
		}

		if (message.getHeader(WWWAuthenticate.NAME) != null) {
			return true;
		}
		if (message.getHeader(PAssertedIdentityHeader.NAME) != null) {
			return true;
		}
		if (message.getHeader(ProxyAuthenticateHeader.NAME) != null) {
			return true;
		}
		if (message.getHeader(ProxyAuthorizationHeader.NAME) != null) {
			return true;
		}
		return false;
	}

	public String getSdp(String message) {
		if (!message.contains("v=0")) {
			return null;
		}
		String[] lines = message.split("\n");
		if (lines == null || lines.length <= 0) {
			return null;
		}

		String sdp = "";
		boolean sdpBegin = false;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("v=0")) {
				sdpBegin = true;
			}
			if (sdpBegin) {
				sdp += lines[i];
			}
		}
		return sdp;
	}

	public static Response createResponseMessage(Request request, int responseCode, String sdpData) {
		try {
			if (request == null) {
				throw new Exception();
			}
			SipServerTransport sipServerTransport = ServerCore.getTransport(request);
			if (sipServerTransport == null) {
				throw new Exception();
			}
			Response response = sipServerTransport.getMessageFactory().createResponse(responseCode, request);
			if (sdpData != null) {
				response.setContent(sdpData.getBytes(), sipServerTransport.getHeaderFactory().createContentTypeHeader("application", "sdp"));
			}
			response.addHeader(sipServerTransport.getHeaderFactory().createAllowHeader(SipServerSharedProperties.allowHeaderValue));
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void addContactHeader(Message message, Extension extension) {
		try {
			SipServerTransport transport = ServerCore.getTransport(extension.getTransportType());
			if (transport == null) {
				throw new Exception();
			}

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
			message.addHeader(contactHeader);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public StackLogger getLogger() {
		return logger;
	}

	public void setLogger(StackLogger logger) {
		this.logger = logger;
	}

	// protected void waitResponse(CallParam callParam) {
	// try {
	// String branch = AliasService.getBranch(callParam.getRequest());
	// ExceptionService.checkNullObject(branch);
	// callParam.setRecievedResponse(false);
	// String lockValue = UUID.randomUUID().toString();
	// lockProperties.put(branch, lockValue);
	// synchronized (lockValue) {
	// lockValue.wait(SipServerSharedProperties.messageTimeout);
	// }
	// if (!callParam.isRecievedResponse()) {
	// ServerCore.getServerCore().getStatusService().busy(callParam);
	// lockProperties.remove(branch);
	// return;
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	// protected void notifyWait(String branch) {
	// if (lockProperties.get(branch) != null) {
	// synchronized (lockProperties.get(branch)) {
	// lockProperties.get(branch).notify();
	// }
	// lockProperties.remove(branch);
	// }
	// }

}
