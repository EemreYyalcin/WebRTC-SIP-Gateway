package sipserver.com.server;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

public class SipAdapter implements SipListener {

	public void processDialogTerminated(DialogTerminatedEvent arg0) {
	}

	public void processIOException(IOExceptionEvent arg0) {
	}

	public void processRequest(RequestEvent arg0) {
	}

	public void processResponse(ResponseEvent arg0) {
	}

	public void processTimeout(TimeoutEvent arg0) {
	}

	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
	}
}
