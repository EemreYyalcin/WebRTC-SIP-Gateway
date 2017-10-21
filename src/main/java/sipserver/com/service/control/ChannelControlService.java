package sipserver.com.service.control;

import java.util.Properties;

import sipserver.com.parameter.param.CallParam;

public class ChannelControlService {

	private static Properties channelList = new Properties();

	public static CallParam getChannel(String callId) {
		return (CallParam) channelList.get(callId);
	}

	public static CallParam takeChannel(String callId) {
		return (CallParam) channelList.remove(callId);
	}

	public static void putChannel(String callId, CallParam callParam) {
		channelList.put(callId, callParam);
	}

}
