package sipserver.com.core.sip.service.operational;
//package sipserver.com.service.operational;
//
//import java.util.Objects;
//
//import javax.sip.message.Response;
//
//import org.apache.log4j.Logger;
//
//import com.mgcp.message.command.commandLine.MGCPCommandLine.MGCPVerb;
//import com.mgcp.message.response.MGCPResponse;
//import com.mgcp.transport.MgcpSession;
//import com.mgcp.transport.MgcpSessionInterface;
//
//import sipserver.com.executer.sip.transaction.ServerTransaction;
//import sipserver.com.parameter.param.CallParam;
//
//public class IvrService {
//
//	private static Logger logger = Logger.getLogger(IvrService.class);
//
//	public static void beginIvrCall(ServerTransaction serverTransaction) {
//
//		try {
//			CallParam fromCallParam = serverTransaction.getCallParam();
//
//			MgcpSessionInterface mgcpSessionInterface = new MgcpSessionInterface() {
//				@Override
//				public void processException(Exception exception) {
//					exception.printStackTrace();
//				}
//
//				@Override
//				public void onSuccess(MGCPResponse mgcpResponse, MGCPVerb verb) {
//					logger.debug("MediaServer sdp Comming !!");
//					logger.debug("SDP: " + mgcpResponse.getSdpInformation());
//					if (verb.equals(MGCPVerb.RQNT)) {
//						logger.error("RQNT Response Taken");
//						return;
//					}
//
//					if (Objects.isNull(mgcpResponse.getSdpInformation())) {
//						return;
//					}
//
//					serverTransaction.sendResponseMessage(Response.OK, mgcpResponse.getSdpInformation());
//					fromCallParam.getMgcpSession().request("C:\\temp\\b.wav", "C:\\temp\\c.wav", "C:\\temp\\d.wav");
//				}
//
//				@Override
//				public void onError(MGCPResponse mgcpResponse, MGCPVerb verb) {
//					logger.debug("MediaServer On Error !!");
//					serverTransaction.sendResponseMessage(Response.DECLINE);
//				}
//			};
//
//			MgcpSession mgcpSession = new MgcpSession(mgcpSessionInterface);
//			fromCallParam.setMgcpSession(mgcpSession);
//
//			mgcpSession.createIVR(fromCallParam.getSdpRemoteContent().trim());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
//
//}
