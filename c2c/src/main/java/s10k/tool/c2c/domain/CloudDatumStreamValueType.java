package s10k.tool.c2c.domain;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A cloud datum stream value type.
 */
public enum CloudDatumStreamValueType {

	/** A value reference to a cloud data source. */
	Reference('r', false),

	/** A SPEL expression. */
	SpelExpression('s', true),

	;

	private final String key;
	private final boolean expression;

	CloudDatumStreamValueType(char key, boolean expression) {
		this.key = String.valueOf(key);
		this.expression = expression;
	}

	/**
	 * Test if this enum represents an expression value type.
	 *
	 * @return {@literal true} if this is an expression value type
	 */
	public final boolean isExpression() {
		return expression;
	}

	/**
	 * Get a key value for this enum.
	 *
	 * @return the key
	 */
	public char toKey() {
		return key.charAt(0);
	}

	/**
	 * Get a key value for this enum.
	 *
	 * @return the key as a string
	 */
	@JsonValue
	public String keyValue() {
		return key;
	}

	/**
	 * Get an enum instance for a key value.
	 *
	 * @param key the key
	 * @return the enum
	 * @throws IllegalArgumentException if {@code key} is not a valid value
	 */
	public static CloudDatumStreamValueType valueOf(char key) {
		for (CloudDatumStreamValueType e : CloudDatumStreamValueType.values()) {
			if (key == e.key.charAt(0)) {
				return e;
			}
		}
		throw new IllegalArgumentException("Unknown CloudDatumStreamValueType key [" + key + "]");
	}

	/**
	 * Get an enum instance for a name or key value.
	 *
	 * @param value the enumeration name or key value, case-insensitve
	 * @return the enum, or {@code null} if value is {@code null} or empty
	 * @throws IllegalArgumentException if {@code value} is not a valid value
	 */
	@JsonCreator
	public static @Nullable CloudDatumStreamValueType fromValue(@Nullable String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		final char key = value.length() == 1 ? Character.toLowerCase(value.charAt(0)) : 0;
		for (CloudDatumStreamValueType e : CloudDatumStreamValueType.values()) {
			if (key == e.key.charAt(0) || value.equalsIgnoreCase(e.name())) {
				return e;
			}
		}
		throw new IllegalArgumentException("Unknown CloudDatumStreamValueType value [" + value + "]");
	}

}
