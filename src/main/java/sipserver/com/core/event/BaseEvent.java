package sipserver.com.core.event;

import javax.sip.message.Request;

public interface BaseEvent {

	public boolean onTrying();

	public boolean onOk();
	
	public boolean onReject(int statusCode);

	public boolean onFinish();
	
	public boolean onFinishImmediately();

	public boolean onBye();
	
	public boolean onBye(Request byeRequest);

	public boolean onRinging();

	public boolean onCancel();

	public boolean onCancel(Request cancelRequest);
	
	public boolean onACK();

}
