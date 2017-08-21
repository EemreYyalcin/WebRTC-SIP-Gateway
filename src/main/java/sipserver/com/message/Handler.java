package sipserver.com.message;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.message.Request;
import sipserver.com.executer.core.ServerCore;

public class Handler {

	private static StackLogger logger = CommonLogger.getLogger(Handler.class);

	public static void createRequestProcess(RequestEvent requestEvent) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (requestEvent.getRequest().getMethod().equals(Request.REGISTER)) {
						ServerCore.getServerCore().getRegisterServiceIn().processRequest(requestEvent);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void createResponseProcess(ResponseEvent responseEvent) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					CSeqHeader cseqHeader = (CSeqHeader) responseEvent.getResponse().getHeader(CSeqHeader.NAME);
					if (cseqHeader == null) {
						return;
					}
					if (cseqHeader.getMethod().equals(Request.REGISTER)) {
						ServerCore.getServerCore().getRegisterServiceOut().processResponse(responseEvent);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

}
