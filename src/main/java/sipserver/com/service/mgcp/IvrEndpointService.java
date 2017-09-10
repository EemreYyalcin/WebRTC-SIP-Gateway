package sipserver.com.service.mgcp;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.Constants;
import jain.protocol.ip.mgcp.message.CreateConnection;
import jain.protocol.ip.mgcp.message.CreateConnectionResponse;
import jain.protocol.ip.mgcp.message.NotificationRequest;
import jain.protocol.ip.mgcp.message.parms.CallIdentifier;
import jain.protocol.ip.mgcp.message.parms.ConnectionDescriptor;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;
import jain.protocol.ip.mgcp.message.parms.ConnectionMode;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.EventName;
import jain.protocol.ip.mgcp.message.parms.RequestedAction;
import jain.protocol.ip.mgcp.message.parms.RequestedEvent;
import jain.protocol.ip.mgcp.pkg.MgcpEvent;
import jain.protocol.ip.mgcp.pkg.PackageName;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.service.MgcpService;
import sipserver.com.service.Service;

public class IvrEndpointService extends MgcpService {

	private static StackLogger logger = CommonLogger.getLogger(IvrEndpointService.class);

	public IvrEndpointService() {
		setEndpointID(new EndpointIdentifier("mobicents/ivr/$", ServerCore.getCoreElement().getMediaServerIp() + ":" + ServerCore.getCoreElement().getMediaServerPort()));
	}

	@Override
	public void transactionEnded(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transactionRxTimedOut(JainMgcpCommandEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transactionTxTimedOut(JainMgcpCommandEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processMgcpCommandEvent(JainMgcpCommandEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createConnection(String messageID, Service service, byte[] sdpData) {
		try {
			CallIdentifier callID = getCaProvider().getUniqueCallIdentifier();
			int transactionHandler = getCaProvider().getUniqueTransactionHandler();
			putMessage(transactionHandler + "", messageID);
			putService(messageID, service);
			CreateConnection createConnection = new CreateConnection(this, callID, getEndpointID(), ConnectionMode.SendRecv);
			createConnection.setTransactionHandle(transactionHandler);
			if (sdpData != null) {
				logger.logFatalError("-----> " + new String(sdpData));
				createConnection.setRemoteConnectionDescriptor(new ConnectionDescriptor(new String(sdpData)));
			}
			getCaProvider().sendMgcpEvents(new JainMgcpEvent[] { createConnection });
			logger.logFatalError(" CreateConnection command sent for TxId " + createConnection.getTransactionHandle() + " and CallId " + callID);

		} catch (Exception e) {
			e.printStackTrace();
		}
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
			int transactionID = response.getTransactionHandle();
			String messageID = takeMessage(transactionID + "");
			Service service = takeService(messageID);
			if (service != null) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						service.mediaServerEvents(jainmgcpresponseevent, messageID);
						playSound(response, transactionID);
					}
				}).start();
			}
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

	private void playSound(CreateConnectionResponse response, int transactionID) {
		NotificationRequest notificationRequest = new NotificationRequest(this, response.getSpecificEndpointIdentifier(), getCaProvider().getUniqueRequestIdentifier());
//		NotificationRequest notificationRequest = new NotificationRequest(this, getEndpointID(), getCaProvider().getUniqueRequestIdentifier());
		notificationRequest.setTransactionHandle(getCaProvider().getUniqueTransactionHandler());
		RequestedAction[] actions = new RequestedAction[] { RequestedAction.NotifyImmediately };
		
		RequestedEvent[] requestedEvents = {
				new RequestedEvent(new EventName(PackageName.Announcement, MgcpEvent.oc, response.getConnectionIdentifier()),
						actions),
				new RequestedEvent(new EventName(PackageName.Announcement, MgcpEvent.of, response.getConnectionIdentifier()),
						actions) };
		
		EventName[] signalRequests = { new EventName(PackageName.Announcement, MgcpEvent.ann.withParm("http://www.music.helsinki.fi/tmt/opetus/uusmedia/esim/a2002011001-e02.wav"), response.getConnectionIdentifier()) };
		
		
		notificationRequest.setRequestedEvents(requestedEvents);
		notificationRequest.setSignalRequests(signalRequests);

		getCaProvider().sendMgcpEvents(new JainMgcpEvent[] { notificationRequest });
	}

}
