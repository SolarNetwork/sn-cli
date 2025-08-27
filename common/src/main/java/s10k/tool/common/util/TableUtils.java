package s10k.tool.common.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper methods for generating formatted tables.
 */
public class TableUtils {

	/**
	 * Generate a basic ASCII table structure for a map.
	 *
	 * @param map               the map
	 * @param keyName           the key header column name
	 * @param valName           the value header column name
	 * @param valRightJustified {@code true} to right-justify values
	 * @return the formatted table
	 */
	public static String basicTable(Map<?, ?> map, String keyName, String valName, boolean valRightJustified) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		int keyWidth = keyName != null ? keyName.length() : 0;
		int valWidth = valName != null ? valName.length() : 0;
		final Map<String, Object> dispMap = new LinkedHashMap<>(map.size());
		for (Map.Entry<?, ?> e : map.entrySet()) {
			String k = e.getKey().toString();
			String v = e.getValue() != null ? e.getValue().toString() : "";

			keyWidth = Math.max(keyWidth, k.length());

			// look for newline split
			if (v.indexOf('\n') > 0) {
				String[] lines = v.split("\\n", 0);
				for (String line : lines) {
					valWidth = Math.max(valWidth, line.length());
				}
				dispMap.put(k, lines);
			} else {
				valWidth = Math.max(valWidth, v.length());
				dispMap.put(k, v);
			}
		}
		final String tmpl = "%-" + keyWidth + "s %" + (valRightJustified ? "" : "-") + valWidth + "s\n";
		StringBuilder buf = new StringBuilder(256);
		if (keyName != null && valName != null && !keyName.isBlank() && !valName.isBlank()) {
			buf.append(String.format(tmpl, keyName, valName));
			for (int i = 0, len = keyWidth + 1 + valWidth; i < len; i++) {
				buf.append('-');
			}
			buf.append('\n');
		}
		for (Map.Entry<String, Object> e : dispMap.entrySet()) {
			Object v = e.getValue();
			if (v instanceof String[] lines) {
				for (int i = 0, len = lines.length; i < len; i++) {
					if (i == 0) {
						buf.append(String.format(tmpl, e.getKey(), lines[i]));
					} else {
						buf.append(String.format(tmpl, "", lines[i]));
					}
				}
			} else {
				buf.append(String.format(tmpl, e.getKey(), e.getValue()));
			}
		}
		return buf.toString();
	}

}
