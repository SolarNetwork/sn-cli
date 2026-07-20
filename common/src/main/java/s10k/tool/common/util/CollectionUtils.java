package s10k.tool.common.util;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringToList;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToMap;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToSet;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Collection utilities.
 */
public final class CollectionUtils {

	private CollectionUtils() {
		// not available
	}

	/**
	 * Populate a service property from a specification.
	 * 
	 * <p>
	 * The following alternate {@code name:value} delimiters are supported:
	 * </p>
	 * 
	 * <ol>
	 * <li><code>{:</code> - treat the value as JSON</li>
	 * <li><code>%:</code> - treat the value as a comma-delimited, equal-delimited
	 * mapping, for example <code>a=b,c=d</code></li>
	 * <li><code>[:</code> - treat the value as a comma-delimited list</li>
	 * <li><code>#:</code> - treat the value as a comma-delimited set</li>
	 * </ol>
	 * 
	 * <p>
	 * If a value starts with an {@code @} character, it will be treated as file
	 * path to load the contents of.
	 * </p>
	 * 
	 * @param spec         a service property specification, in the form path:value
	 * 
	 * @param sprops       the service properties map to populate
	 * @param objectMapper an optional object mapper, to support path{:value syntax
	 *                     to treat {@code value} as JSON
	 */
	public static void populateServiceProperty(final String spec, final Map<String, Object> sprops,
			final @Nullable ObjectMapper objectMapper) {
		final int colonIdx = spec.indexOf(':');
		if (colonIdx < 1) {
			throw new IllegalArgumentException(
					"Invalid service property [%s], must be in the form name:value.".formatted(spec));
		}
		final String specKey = spec.substring(0, colonIdx);
		final String specValue = stringOrFileContents(spec.substring(colonIdx + 1));
		final String path;
		final Object value;
		if (objectMapper != null && specKey.length() > 2 && specKey.endsWith("{")) {
			// parse value as JSON
			path = specKey.substring(0, specKey.length() - 2);
			try {
				value = objectMapper.readTree(specValue);
			} catch (Exception e) {
				throw new IllegalStateException("Error parsing service property [%s] value [%s] as JSON: %s"
						.formatted(path, specValue, e.getMessage()), e);
			}
		} else if (specKey.length() > 2 && specKey.endsWith("%")) {
			// parse value as delimited map
			path = specKey.substring(0, specKey.length() - 2);
			value = commaDelimitedStringToMap(specValue);
		} else if (specKey.length() > 2 && specKey.endsWith("[")) {
			// parse value as delimited list
			path = specKey.substring(0, specKey.length() - 2);
			value = commaDelimitedStringToList(specValue);
		} else if (specKey.length() > 2 && specKey.endsWith("#")) {
			// parse value as delimited set
			path = specKey.substring(0, specKey.length() - 2);
			value = commaDelimitedStringToSet(specValue);
		} else {
			path = specKey;
			value = spec.substring(colonIdx + 1);
		}

		// split on slash, dropping any leading slash
		String[] pathSegments = (path.startsWith("/") ? path.substring(1) : path).split("/", 0);
		Map<String, Object> mapToPopulate = sprops;
		for (int i = 1; i < pathSegments.length; i++) {
			Map<String, Object> subMap = new LinkedHashMap<>(2);
			mapToPopulate.put(pathSegments[i - 1], subMap);
			mapToPopulate = subMap;
		}
		mapToPopulate.put(pathSegments[pathSegments.length - 1], value);
	}

	/**
	 * Recursively copy service properties from one map to another.
	 * 
	 * @param input  the input map
	 * @param output the output map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void copyServiceProperties(final @Nullable Map<String, Object> input,
			final Map<String, Object> output) {
		if (input == null) {
			return;
		}
		for (Entry<String, Object> e : input.entrySet()) {
			final String key = e.getKey();
			final Object val = e.getValue();
			if (val instanceof Map<?, ?> m) {
				Map<String, Object> targetMap = (Map) output.compute(key,
						(_, v) -> v instanceof Map<?, ?> t ? (Map) t : new LinkedHashMap<>(8));
				copyServiceProperties((Map) m, targetMap);
			} else {
				output.put(key, val);
			}
		}
	}

}
