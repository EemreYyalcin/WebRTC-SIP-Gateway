package sipserver.com.util.log;

import sipserver.com.domain.Extension;

public class LogTest {

	public static void log(Extension extension, String message) {
		System.out.println("EXXTEN:" + extension.getExten() + " - " + message);
	}
	
	public static void log(String message) {
		System.out.println(" - " + message);
	}
	
	
	
	
}
