package sipserver.com.util.converter;

import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public class Converter {

	public static Stream<Object> getKeyList(Properties properties) {
		try {
			synchronized (properties) {
				Set<Object> keys = properties.keySet();
				if (Objects.isNull(keys) || keys.size() == 0) {
					return null;
				}
				return keys.stream();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
