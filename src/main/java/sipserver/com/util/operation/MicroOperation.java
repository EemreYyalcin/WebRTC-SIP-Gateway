package sipserver.com.util.operation;

import java.util.Objects;

public class MicroOperation {

	public static boolean isAnyNull(Object... objects) {
		for (Object object : objects) {
			if (Objects.isNull(object)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isAnyNotNull(Object... objects) {
		return !isAnyNull(objects);
	}
	
	
}
