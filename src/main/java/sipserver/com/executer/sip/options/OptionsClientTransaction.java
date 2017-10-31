package sipserver.com.executer.sip.options;

import java.util.Objects;

import javax.sip.header.ToHeader;
import javax.sip.message.Response;

import sipserver.com.domain.Extension;
import sipserver.com.domain.ExtensionBuilder;
import sipserver.com.executer.sip.transaction.ClientTransaction;

public class OptionsClientTransaction extends ClientTransaction {

	@Override
	public void processResponse(Response response) {
		try {
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			Objects.requireNonNull(toHeader);
			Extension extension = ExtensionBuilder.getExtension(toHeader);
			Objects.requireNonNull(extension);
			setResponse(response);
			sendACK();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
