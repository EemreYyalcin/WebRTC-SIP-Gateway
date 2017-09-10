package sipserver.com.service.mgcp;

import java.net.InetAddress;

import org.mobicents.protocols.mgcp.handlers.MessageHandler;
import org.mobicents.protocols.mgcp.stack.JainMgcpExtendedListener;
import org.mobicents.protocols.mgcp.stack.JainMgcpStackImpl;
import org.mobicents.protocols.mgcp.stack.JainMgcpStackProviderImpl;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.Constants;
import jain.protocol.ip.mgcp.message.CreateConnection;
import jain.protocol.ip.mgcp.message.CreateConnectionResponse;
import jain.protocol.ip.mgcp.message.parms.CallIdentifier;
import jain.protocol.ip.mgcp.message.parms.ConnectionDescriptor;
import jain.protocol.ip.mgcp.message.parms.ConnectionMode;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;

public class MgcpControlService implements JainMgcpExtendedListener {

	private static StackLogger logger = CommonLogger.getLogger(MgcpControlService.class);

	private JainMgcpStackProviderImpl caProvider;
	private static final int CA_PORT = 2428;
	private static final int MGW_PORT = 2427;
	private static final String MGW_ADDRESS = "192.168.1.102";

	private InetAddress caIPAddress = null;
	private JainMgcpStackImpl caStack = null;

	private JainMgcpStackImpl stack;
	private MessageHandler handler;
	private EndpointIdentifier endpointID;

	public void setUp() {
		try {
			// System.setProperty("", "");
			// System.setProperty("", "");
			stack = new JainMgcpStackImpl();
			handler = new MessageHandler(stack);
			caIPAddress = InetAddress.getByName("192.168.1.106");
			caStack = new JainMgcpStackImpl(caIPAddress, CA_PORT);
			caProvider = (JainMgcpStackProviderImpl) caStack.createProvider();
			caProvider.addJainMgcpListener(this);
			// endpointID = new EndpointIdentifier("restcomm/ivr/$", MGW_ADDRESS + ":" +
			// MGW_PORT);
			endpointID = new EndpointIdentifier("mobicents/ivr/$", MGW_ADDRESS + ":" + MGW_PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createConnection(String sdpData) {
		try {
			CallIdentifier callID = caProvider.getUniqueCallIdentifier();

			CreateConnection createConnection = new CreateConnection(this, callID, endpointID, ConnectionMode.SendRecv);

			if (sdpData != null) {
				createConnection.setRemoteConnectionDescriptor(new ConnectionDescriptor(sdpData));
			}

			createConnection.setTransactionHandle(caProvider.getUniqueTransactionHandler());
			caProvider.sendMgcpEvents(new JainMgcpEvent[] { createConnection });
			logger.logFatalError(" CreateConnection command sent for TxId " + createConnection.getTransactionHandle() + " and CallId " + callID);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void processMgcpCommandEvent(JainMgcpCommandEvent jainmgcpcommandevent) {
		logger.logFatalError("processMgcpCommandEvent " + jainmgcpcommandevent);
	}

	@Override
	public void processMgcpResponseEvent(JainMgcpResponseEvent jainmgcpresponseevent) {
		logger.logFatalError("processMgcpResponseEvent = " + jainmgcpresponseevent);
		switch (jainmgcpresponseevent.getObjectIdentifier()) {
		case Constants.RESP_CREATE_CONNECTION:
			CreateConnectionResponse response = (CreateConnectionResponse) jainmgcpresponseevent;
			logger.logFatalError("CreateConnectionResponse:");
			logger.logFatalError(response.toString());
			logger.logFatalError("Response End");
			logger.logFatalError("STARTED->");
			logger.logFatalError(getSdp(response.toString()));
			logger.logFatalError("ENDED->");

			// if (response.getReturnCode().getValue() ==
			// ReturnCode.ENDPOINT_UNKNOWN ||
			// response.getReturnCode().getValue() ==
			// ReturnCode.TRANSACTION_EXECUTED_NORMALLY) {
			//
			// }
			break;
		default:
			logger.logFatalError("This RESPONSE is unexpected " + jainmgcpresponseevent);
			// CreateConnectionTest.fail("Incorrect response for CRCX
			// command ");
			break;

		}

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

	@Override
	public void transactionRxTimedOut(JainMgcpCommandEvent command) {
		logger.logFatalError("transactionRxTimedOut " + command);
	}

	@Override
	public void transactionTxTimedOut(JainMgcpCommandEvent command) {
		logger.logFatalError("transactionTxTimedOut " + command);
	}

	@Override
	public void transactionEnded(int handle) {
		// TODO Auto-generated method stub
	}


}
