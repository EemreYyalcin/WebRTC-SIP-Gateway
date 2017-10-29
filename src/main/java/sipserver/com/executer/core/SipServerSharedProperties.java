package sipserver.com.executer.core;

import javax.sip.message.Request;

public class SipServerSharedProperties {

	public static int errorResponseCode = 911;

	public static int errorCode = -1;

	public static int blankCode = 0;
	
	public static int messageTimeout = 2 * 1000;
	
	public static int registerSendingIntervallForUnRegisterExten = 4 * 1000;

	public static int registerSendingIntervallForRegisterExten = 60 * 1000;

	public static int optionsSendingIntervallForRegisterExten = 15 * 1000;

	public static int extensionControlServiceControlInterval = 1000;

	public static String allowHeaderValue = Request.INVITE + "," + Request.OPTIONS + "," + Request.BYE + "," + Request.REGISTER + "," + Request.ACK + "," + Request.CANCEL + "," + Request.INFO + "," + Request.MESSAGE + "," + Request.NOTIFY + "," + Request.SUBSCRIBE;

	public static long ringTimeout = 120 * 1000;

	public static int tryingTimeoutCount = 5;


}
