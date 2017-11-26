package sipserver.com.service.operational;

import java.util.Arrays;
import java.util.Objects;

import javax.sip.header.FromHeader;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import sipserver.com.executer.core.ServerCore;
import sipserver.com.executer.sip.transaction.ClientTransaction;
import sipserver.com.executer.sip.transaction.ServerTransaction;
import sipserver.com.executer.sip.transaction.Transaction;

public class BridgeService {

	private static Logger logger = Logger.getLogger(BridgeService.class);

	public static void observeTransaction(Transaction transaction, int responseCode) {
		observeTransaction(transaction, responseCode, null);
	}

	public static void observeTransaction(Transaction transaction, int responseCode, String sdp) {
		try {
			if (Objects.isNull(transaction)) {
				return;
			}

			if (transaction.predicateLastResponseStateList.test(Arrays.asList(Response.INTERVAL_TOO_BRIEF, Response.REQUEST_TERMINATED))) {
				if (transaction instanceof ServerTransaction) {
					responseCode = Response.INTERVAL_TOO_BRIEF;
				} else {
					responseCode = Response.REQUEST_TERMINATED;
				}
			}

			Transaction bridgeTransaction = transaction.getBridgeTransaction();
			if (transaction instanceof ServerTransaction) {
				observeServerTransaction((ServerTransaction) transaction, responseCode, sdp);
				if (Objects.nonNull(bridgeTransaction)) {
					observeClientTransaction((ClientTransaction) bridgeTransaction, responseCode, sdp);
				}
			} else {
				observeClientTransaction((ClientTransaction) transaction, responseCode, sdp);
				if (Objects.nonNull(bridgeTransaction)) {
					observeServerTransaction((ServerTransaction) bridgeTransaction, responseCode, sdp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void observeServerTransaction(ServerTransaction fromTransaction, int responseCode, String sdp) {
		try {
			fromTransaction.setCurrentResponseCode(responseCode);
			if (responseCode == Response.RINGING) {
				fromTransaction.sendResponseMessage(responseCode, sdp);
				return;
			}

			if (responseCode == Response.SERVER_INTERNAL_ERROR) {
				fromTransaction.sendResponseMessage(responseCode); // and wait ACK
				return;
			}

			if (responseCode == Response.REQUEST_TIMEOUT) {
				fromTransaction.sendResponseMessage(responseCode); // and wait ACK
				return;
			}

			if (responseCode == Response.INTERVAL_TOO_BRIEF) {
				// If FirstBye Recive From Server Transaction
				processServerOrClientTransactionByeRequest(fromTransaction);
				return;
			}

			if (responseCode == Response.REQUEST_TERMINATED) {
				// If FirstBye Recive From Client Transaction
				fromTransaction.sendByeMessage();
				return;
			}

			if (responseCode == Response.TEMPORARILY_UNAVAILABLE) {
				Response response = ServerCore.getCoreElement().getMessageFactory().createResponse(Response.OK, fromTransaction.getCancelRequest());
				fromTransaction.sendResponseMessage(response);
				// For Cancel
				Response responseRequestTerminated = ServerCore.getCoreElement().getMessageFactory().createResponse(Response.REQUEST_TERMINATED, fromTransaction.getRequest());
				fromTransaction.sendResponseMessage(responseRequestTerminated); // and wait ACK
				return;
			}

			if (responseCode == Response.BUSY_HERE) {
				// TODO: Wait ACK ???
				fromTransaction.sendResponseMessage(responseCode);
				return;
			}

			if (responseCode == Response.DECLINE) {
				// TODO: Wait ACK ???
				fromTransaction.sendResponseMessage(responseCode);
				return;
			}

			if (responseCode == Response.OK) {
				fromTransaction.sendResponseMessage(responseCode, sdp); // and wait ACK
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void observeClientTransaction(ClientTransaction toTransaction, int responseCode, String sdp) {
		try {
			toTransaction.setCurrentResponseCode(responseCode);

			if (responseCode == Response.RINGING) {
				// TODO: Make Anything :)
				return;
			}

			if (responseCode == Response.SERVER_INTERNAL_ERROR) {
				processClientTransactionError(toTransaction);
				return;
			}

			if (responseCode == Response.REQUEST_TERMINATED) {
				// If FirstBye Recive From Client Transaction
				processServerOrClientTransactionByeRequest(toTransaction);
				return;
			}

			if (responseCode == Response.INTERVAL_TOO_BRIEF) {
				// If FirstBye Recive From Server Transaction
				toTransaction.sendByeMessage();
				return;
			}

			if (responseCode == Response.TEMPORARILY_UNAVAILABLE) {
				toTransaction.sendCancelMessage();
				return;
			}

			if (responseCode == Response.BUSY_HERE) {
				toTransaction.sendACK();
				return;
			}

			if (responseCode == Response.DECLINE) {
				toTransaction.sendACK();
				return;
			}

			if (responseCode == Response.OK) {
				toTransaction.sendACK();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processClientTransactionError(ClientTransaction toTransaction) {
		if (toTransaction.getLastResponseCode() == Response.OK) {
			// Call Process
			observeClientTransaction(toTransaction, Response.REQUEST_TERMINATED, null);
			return;
		}

		if (toTransaction.getLastResponseCode() == Response.RINGING || toTransaction.getLastResponseCode() == Response.TRYING) {
			// Call Starting Process
			observeClientTransaction(toTransaction, Response.TEMPORARILY_UNAVAILABLE, null);
			return;
		}

	}

	private static void processServerOrClientTransactionByeRequest(Transaction transaction) {
		try {
			if (Objects.isNull(transaction.getCallParam())) {
				logger.error("Channel Not Found " + transaction.getByeRequest().getHeader(FromHeader.NAME).toString());
				return;
			}
			Response response = ServerCore.getCoreElement().getMessageFactory().createResponse(Response.OK, transaction.getByeRequest());
			transaction.getTransport().sendSipMessage(response, transaction.getAddress(), transaction.getPort(), transaction.getSession());
			// waitACK

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
