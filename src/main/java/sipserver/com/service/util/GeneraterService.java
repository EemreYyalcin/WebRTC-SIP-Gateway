package sipserver.com.service.util;

import java.util.UUID;

public class GeneraterService {

	public static String getUUid(int size) {
		return UUID.randomUUID().toString().substring(0, size);
	}

	public static String getUUidForBranch() {
		return "z9hG4bK-" + UUID.randomUUID().toString();
	}

	public static String getUUidForTag() {
		return getUUid(6);
	}

}
