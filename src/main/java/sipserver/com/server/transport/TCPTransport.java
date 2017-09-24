package sipserver.com.server.transport;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import sipserver.com.parameter.constant.ParamConstant.TransportType;
import sipserver.com.server.SipServerTransport;

public class TCPTransport extends SipServerTransport {

	// Logger
	private static StackLogger logger = CommonLogger.getLogger(TCPTransport.class);

	public TCPTransport(String host, int port) {
		super(host, port, TransportType.TCP, logger);
	}

}
