package sipserver.com.service.util;

public class ExceptionService {

	public static void checkNullObject(Object object) throws Exception {
		if (object == null) {
			throw new Exception();
		}
	}

}
