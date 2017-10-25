package sipserver.com.service.operational;

import java.util.Objects;

import javax.sip.message.Request;

import sipserver.com.domain.Extension;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.TransactionBuilder;

public class RegisterService {

	public static void register(Extension extTrunk) {
		try {
			Request requestMessage = ClientTransaction.createRequestMessage(Request.REGISTER, extTrunk);
			ClientTransaction clientTransaction = TransactionBuilder.createAndStartClientTransaction(requestMessage, extTrunk.getAddress(), extTrunk.getPort(), extTrunk.getTransport());
			Objects.requireNonNull(clientTransaction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
