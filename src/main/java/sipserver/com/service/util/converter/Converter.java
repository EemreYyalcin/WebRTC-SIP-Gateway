package sipserver.com.service.util.converter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class Converter {

	public static ArrayList<String> getKeyList(Properties properties) {
		try {
			ArrayList<String> extenList = null;
			synchronized (properties) {
				Set<Object> keys = properties.keySet();
				if (Objects.isNull(keys) || keys.size() == 0) {
					return null;
				}
				for (Object key : keys) {
					if (Objects.isNull(extenList)) {
						extenList = new ArrayList<String>();
					}
					extenList.add(new String((String) key));
				}
			}
			return extenList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
