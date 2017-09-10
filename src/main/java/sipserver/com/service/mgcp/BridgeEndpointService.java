package sipserver.com.service.mgcp;

import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import sipserver.com.executer.core.ServerCore;
import sipserver.com.service.MgcpService;
import sipserver.com.service.Service;

public class BridgeEndpointService extends MgcpService {

	public BridgeEndpointService() {
		setEndpointID(new EndpointIdentifier("mobicents/bridge/$", ServerCore.getCoreElement().getMediaServerIp() + ":" + ServerCore.getCoreElement().getMediaServerPort()));
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
	public void processMgcpResponseEvent(JainMgcpResponseEvent arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void createConnection(String callID, Service service, byte[] sdpData) {
		// TODO Auto-generated method stub
		
	}

}
