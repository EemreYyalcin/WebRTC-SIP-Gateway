package sipserver.com.service.operational;

import java.util.Objects;

import javax.sip.message.Request;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.TransactionBuilder;

public class OptionsService {

	public static void pingExtension(Extension extTrunk) {
		try {
			Request requestMessage = ClientTransaction.createRequestMessage(Request.OPTIONS, extTrunk);
			Objects.requireNonNull(extTrunk.getTransport());
			ClientTransaction clientTransaction = TransactionBuilder.createAndStartClientTransaction(requestMessage, extTrunk.getAddress(), extTrunk.getPort(), extTrunk.getTransport());
			Objects.requireNonNull(clientTransaction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
