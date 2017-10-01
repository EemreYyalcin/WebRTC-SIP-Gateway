package sipserver.com.service.control;

import java.util.ArrayList;
import java.util.Properties;

import javax.sip.Transaction;

import sipserver.com.parameter.param.CallParam;

public class ChannelControlService {

	private Properties channelList = new Properties();

	public CallParam getChannel(String exten, Transaction transaction) {
		if (channelList.get(exten) == null) {
			return null;
		}
		if (!(channelList.get(exten) instanceof ArrayList<?>)) {
			return (CallParam) channelList.get(exten);
		}
		ArrayList<CallParam> extenChannelList = (ArrayList<CallParam>) channelList.get(exten);
		for (int i = 0; i < extenChannelList.size(); i++) {
			if (extenChannelList.get(i).getTransaction() == transaction) {
				return extenChannelList.get(i);
			}
		}
		return null;
	}

	public CallParam takeChannel(String exten, Transaction transaction) {
		if (channelList.get(exten) == null) {
			return null;
		}
		if (!(channelList.get(exten) instanceof ArrayList<?>)) {
			return (CallParam) channelList.remove(exten);
		}
		ArrayList<CallParam> extenChannelList = (ArrayList<CallParam>) channelList.get(exten);
		for (int i = 0; i < extenChannelList.size(); i++) {
			if (extenChannelList.get(i).getTransaction() == transaction) {
				return extenChannelList.remove(i);
			}
		}
		return null;
	}

	public void putChannel(String exten, CallParam callParam) {
		if (channelList.get(exten) == null) {
			channelList.put(exten, callParam);
			return;
		}
		if (!(channelList.get(exten) instanceof ArrayList<?>)) {
			ArrayList<CallParam> extenChannelList = new ArrayList<CallParam>();
			extenChannelList.add((CallParam) channelList.get(exten));
			extenChannelList.add(callParam);
			channelList.put(exten, extenChannelList);
			return;
		}
		ArrayList<CallParam> extenChannelList = (ArrayList<CallParam>) channelList.get(exten);
		extenChannelList.add(callParam);
	}

}
