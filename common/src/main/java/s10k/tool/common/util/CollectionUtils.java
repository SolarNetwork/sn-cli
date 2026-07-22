package s10k.tool.common.util;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringToList;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToMap;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToSet;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.codec.JsonUtils;
import s10k.tool.common.domain.MergeMode;

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
	 * @param serviceProps the service properties map to populate
	 * @param objectMapper an optional object mapper, to support path{:value syntax
	 *                     to treat {@code value} as JSON
	 */
	public static void populateServiceProperty(final String spec, final Map<String, Object> serviceProps,
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
			path = specKey.substring(0, specKey.length() - 1);
			try {
				JsonNode json = objectMapper.readTree(specValue);
				value = (json.isNull() ? null : json);
			} catch (Exception e) {
				throw new IllegalStateException("Error parsing service property [%s] value [%s] as JSON: %s"
						.formatted(path, specValue, e.getMessage()), e);
			}
		} else if (specKey.length() > 2 && specKey.endsWith("%")) {
			// parse value as delimited map
			path = specKey.substring(0, specKey.length() - 1);
			value = commaDelimitedStringToMap(specValue);
		} else if (specKey.length() > 2 && specKey.endsWith("[")) {
			// parse value as delimited list
			path = specKey.substring(0, specKey.length() - 1);
			value = commaDelimitedStringToList(specValue);
		} else if (specKey.length() > 2 && specKey.endsWith("#")) {
			// parse value as delimited set
			path = specKey.substring(0, specKey.length() - 1);
			value = commaDelimitedStringToSet(specValue);
		} else {
			path = specKey;
			value = spec.substring(colonIdx + 1);
		}

		// split on slash, dropping any leading slash
		String[] pathSegments = (path.startsWith("/") ? path.substring(1) : path).split("/", 0);
		Map<String, Object> mapToPopulate = serviceProps;
		for (int i = 1; i < pathSegments.length; i++) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Map<String, Object> subMap = (Map) mapToPopulate.compute(pathSegments[i - 1],
					(_, v) -> v instanceof Map<?, ?> t ? (Map) t : new LinkedHashMap<>(2));
			mapToPopulate = subMap;
		}
		if (value == null) {
			mapToPopulate.remove(pathSegments[pathSegments.length - 1]);
		} else {
			mapToPopulate.put(pathSegments[pathSegments.length - 1], value);
		}
	}

	/**
	 * Parse a list of service property specifications and populate the results onto
	 * a settings map.
	 * 
	 * @param specifications the service specifications to parse
	 * @param serviceProps   the settings map to populate the service properties on
	 * @param objectMapper   the object mapper to parse JSON values with
	 * @throws IllegalStateException if any error parsing the specifications occurs
	 */
	public static void populateServiceProperties(String @Nullable [] specifications, Map<String, Object> serviceProps,
			ObjectMapper objectMapper) {
		if (specifications == null || specifications.length < 1) {
			return;
		}
		for (String spec : specifications) {
			if (spec.startsWith("@")) {
				try {
					Map<String, Object> m = objectMapper.readValue(stringOrFileContents(spec),
							JsonUtils.STRING_MAP_TYPE);
					if (m != null) {
						serviceProps.putAll(m);
					}
				} catch (IOException e) {
					throw new IllegalStateException("Error reading service property JSON: %s".formatted(e.getMessage()),
							e);
				}
			} else {
				populateServiceProperty(spec, serviceProps, objectMapper);
			}
		}
	}

	/**
	 * Recursively copy service properties from one map to another.
	 * 
	 * @param input  the input map
	 * @param output the output map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void mergeServiceProperties(final @Nullable Map<String, Object> input,
			final Map<String, Object> output, MergeMode mode) {
		if (input == null) {
			return;
		}
		for (Entry<String, Object> e : input.entrySet()) {
			final String key = e.getKey();
			final Object val = e.getValue();
			if (val instanceof Map<?, ?> m) {
				Map<String, Object> targetMap = (Map) output.compute(key, (_,
						v) -> mode != MergeMode.Simple && v instanceof Map<?, ?> t ? (Map) t : new LinkedHashMap<>(8));
				mergeServiceProperties((Map) m, targetMap, mode);
			} else if (val instanceof Collection<?> l) {
				Collection<Object> targetList = (List) output.compute(key,
						(_, v) -> mode == MergeMode.RecursiveObjectsAndArrays && v instanceof Collection<?> c
								? (Collection) c
								: new ArrayList<>(2));
				targetList.addAll(l);
			} else {
				output.put(key, val);
			}
		}
	}

}
