package sipserver.com.executer.sip.options;

import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ServerTransaction;

public class OptionsServerTransaction extends ServerTransaction {

	public OptionsServerTransaction(Extension extension) {
		super(extension);
	}

	@Override
	public void processRequest() {
		try {
			debug("Keep Alive Exten:" + getExtension().getExten());
			sendResponseMessage(Response.OK);
			getExtension().keepRegistered();
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
