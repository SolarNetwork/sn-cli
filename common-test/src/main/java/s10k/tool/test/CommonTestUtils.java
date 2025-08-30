package s10k.tool.test;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.FileCopyUtils;

/**
 * Common testing utilities.
 */
public class CommonTestUtils {

	/** A random number generator. */
	public static final SecureRandom RNG = new SecureRandom();

	/** The default maximum scale. */
	public static final int DEFAULT_MAX_SCALE = 9;

	/**
	 * Get a random decimal number.
	 *
	 * @return the random decimal number
	 */
	public static BigDecimal randomDecimal() {
		return new BigDecimal(RNG.nextDouble(-1000.0, 1000.0)).setScale(4, RoundingMode.HALF_UP);
	}

	/**
	 * Get a random string value.
	 *
	 * @return the string
	 */
	public static String randomString() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 14);
	}

	/**
	 * Get a random string value of an arbitrary length.
	 *
	 * @return the string
	 */
	public static String randomString(int len) {
		StringBuilder buf = new StringBuilder();
		while (buf.length() < len) {
			buf.append(UUID.randomUUID().toString().replace("-", ""));
		}
		buf.setLength(len);
		return buf.toString();
	}

	/**
	 * Get a random positive integer value.
	 *
	 * @return the integer
	 */
	public static Integer randomInt() {
		return RNG.nextInt(1, Integer.MAX_VALUE);
	}

	/**
	 * Get a random positive long value.
	 *
	 * @return the long
	 */
	public static Long randomLong() {
		return RNG.nextLong(1, Long.MAX_VALUE);
	}

	/**
	 * Get a random boolean value.
	 *
	 * @return the boolean
	 */
	public static boolean randomBoolean() {
		return RNG.nextBoolean();
	}

	/**
	 * Get 16 random bytes.
	 *
	 * @return the random bytes
	 */
	public static byte[] randomBytes() {
		return randomBytes(16);
	}

	/**
	 * Get random bytes.
	 *
	 * @param len the desired number of bytes
	 * @return random bytes, of length {@code len}
	 * @since 1.5
	 */
	public static byte[] randomBytes(int len) {
		byte[] bytes = new byte[len];
		RNG.nextBytes(bytes);
		return bytes;
	}

	/**
	 * Load a UTF-8 string classpath resource.
	 *
	 * @param resource the resource to load
	 * @param clazz    the class from which to load the resource
	 * @return the resource
	 * @throws RuntimeException if any error occurs
	 */
	public static String utf8StringResource(String resource, Class<?> clazz) {
		try {
			return FileCopyUtils.copyToString(new InputStreamReader(clazz.getResourceAsStream(resource), UTF_8));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generate a basic ASCII table structure for a map.
	 *
	 * @param map     the map
	 * @param keyName the key header column name
	 * @param valName the value header column name
	 * @return the formatted table
	 */
	public static String basicTable(Map<?, ?> map, String keyName, String valName) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		int keyWidth = keyName != null ? keyName.length() : 0;
		int valWidth = valName != null ? valName.length() : 0;
		boolean valRightJustified = false;
		final Map<String, String> dispMap = new LinkedHashMap<>(map.size());
		for (Map.Entry<?, ?> e : map.entrySet()) {
			String k = e.getKey().toString();
			String v = e.getValue().toString();
			keyWidth = Math.max(keyWidth, k.length());
			valWidth = Math.max(valWidth, v.length());
			dispMap.put(k, v);
			if (!valRightJustified && e.getValue() instanceof Number) {
				valRightJustified = true;
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
		for (Map.Entry<String, String> e : dispMap.entrySet()) {
			buf.append(String.format(tmpl, e.getKey(), e.getValue()));
		}
		return buf.toString();
	}

}
