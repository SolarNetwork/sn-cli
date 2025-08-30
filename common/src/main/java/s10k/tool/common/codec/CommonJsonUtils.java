package s10k.tool.common.codec;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Common JSON helpers.
 */
public final class CommonJsonUtils {

	private CommonJsonUtils() {
		// not available
	}

	/** A type reference for a Map with string keys. */
	public static final TypeReference<LinkedHashMap<String, Map<String, Object>>> STRING_MAP_MAP_TYPE = new StringMapMapTypeReference();

	private static final class StringMapMapTypeReference
			extends TypeReference<LinkedHashMap<String, Map<String, Object>>> {

		private StringMapMapTypeReference() {
			super();
		}

	}

	/** A type reference for a Set of strings. */
	public static final TypeReference<LinkedHashSet<String>> STRING_SET_TYPE = new StringSetTypeReference();

	private static final class StringSetTypeReference extends TypeReference<LinkedHashSet<String>> {

		private StringSetTypeReference() {
			super();
		}

	}

}
