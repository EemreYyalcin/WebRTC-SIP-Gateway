package sipserver.com.util.log;

import sipserver.com.domain.Extension;
import sipserver.com.parameter.param.CallParam;

public class LogTest {

	public static void log(Extension extension, String message) {
		System.out.println("EXXTEN:" + extension.getExten() + " - " + message);
	}

	public static void log(String message) {
		System.out.println(" - " + message);
	}

	public static void log(CallParam callParam, String message) {
		String fromExten = "";
		String toExten = "";

		if (callParam != null) {
			if (callParam.getExtension() != null) {
				fromExten = callParam.getExtension().getExten();
			}
		}
		System.out.println(fromExten + " - " + toExten + " -- " + message);
	}

}
