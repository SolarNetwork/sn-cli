package s10k.tool.c2c.domain;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.solarnetwork.util.StringUtils;

/**
 * A cloud data hierarchy component.
 *
 * <p>
 * This is used to represent the cloud data values that can be mapped into a
 * datum stream.
 * </p>
 */
@RegisterReflectionForBinding
@JsonPropertyOrder({ "name", "reference", "identifiers", "metadata", "children" })
public final class CloudDataValue implements Serializable, Comparable<CloudDataValue> {

	@Serial
	private static final long serialVersionUID = 782616385882360558L;

	/** A wildcard identifier value. */
	public static final String WILDCARD_IDENTIFIER = "*";

	private final List<String> identifiers;
	private final String name;
	private final @Nullable String reference;
	private final @Nullable Map<String, ?> metadata;
	private final @Nullable Collection<CloudDataValue> children;

	/**
	 * Generate a path-like reference value out of a list of identifiers.
	 *
	 * @param identifiers the identifiers
	 * @return the reference value, never {@code null}
	 */
	public static String pathReferenceValue(@Nullable Collection<String> identifiers) {
		var buf = new StringBuilder();
		if (identifiers != null && !identifiers.isEmpty()) {
			for (String ident : identifiers) {
				buf.append('/').append(ident);
			}
		}
		if (buf.isEmpty()) {
			buf.append('/');
		}
		return buf.toString();
	}

	/**
	 * Create a new data value instance.
	 *
	 * <p>
	 * The {@code reference} will be set to a path-like value using the
	 * {@code identifiers} components.
	 * </p>
	 *
	 * @param identifiers the value identifiers, unique within the overall hierarchy
	 * @param name        the component name
	 * @throws IllegalArgumentException if {@code identifiers} or {@code name} is
	 *                                  {@code null}
	 */
	public static CloudDataValue dataValue(List<String> identifiers, String name) {
		return dataValue(identifiers, name, null);
	}

	/**
	 * Create a new data value instance.
	 *
	 * <p>
	 * The {@code reference} will be set to a path-like value using the
	 * {@code identifiers} components.
	 * </p>
	 *
	 * @param identifiers the value identifiers, unique within the overall hierarchy
	 * @param name        the component name
	 * @param metadata    the metadata
	 * @throws IllegalArgumentException if {@code identifiers} or {@code name} is
	 *                                  {@code null}
	 */
	public static CloudDataValue dataValue(List<String> identifiers, String name, @Nullable Map<String, ?> metadata) {
		return new CloudDataValue(identifiers, name, pathReferenceValue(identifiers), metadata);
	}

	/**
	 * Create a new data value instance without any {@code reference} value.
	 *
	 * @param identifiers the value identifiers, unique within the overall hierarchy
	 * @param name        the component name
	 * @param metadata    the metadata
	 * @throws IllegalArgumentException if {@code identifiers} or {@code name} is
	 *                                  {@code null}
	 */
	public static CloudDataValue intermediateDataValue(List<String> identifiers, String name,
			@Nullable Map<String, ?> metadata) {
		return new CloudDataValue(identifiers, name, null, metadata);
	}

	/**
	 * Create a new data value instance without any {@code reference} value.
	 *
	 * @param identifiers the value identifiers, unique within the overall hierarchy
	 * @param name        the component name
	 * @param metadata    the metadata
	 * @param children    the optional children values
	 * @throws IllegalArgumentException if {@code identifiers} or {@code name} is
	 *                                  {@code null}
	 */
	public static CloudDataValue intermediateDataValue(List<String> identifiers, String name,
			@Nullable Map<String, ?> metadata, @Nullable Collection<CloudDataValue> children) {
		return new CloudDataValue(identifiers, name, null, metadata, children);
	}

	/**
	 * Create a new data value instance.
	 *
	 * <p>
	 * The {@code reference} will be set to a path-like value using the
	 * {@code identifiers} components.
	 * </p>
	 *
	 * @param identifiers the value identifiers, unique within the overall hierarchy
	 * @param name        the component name
	 * @param metadata    the metadata
	 * @param children    the optional children values
	 * @throws IllegalArgumentException if {@code identifiers} or {@code name} is
	 *                                  {@code null}
	 */
	public static CloudDataValue dataValue(List<String> identifiers, String name, @Nullable Map<String, ?> metadata,
			@Nullable Collection<CloudDataValue> children) {
		return new CloudDataValue(identifiers, name, pathReferenceValue(identifiers), metadata, children);
	}

	/**
	 * Create a new data value instance.
	 *
	 * @param identifiers the value identifiers, unique within the overall hierarchy
	 * @param name        the component name
	 * @param reference   the unique hierarchy reference
	 * @param metadata    the metadata
	 * @throws IllegalArgumentException if {@code identifiers} or {@code name} is
	 *                                  {@code null}
	 */
	public static CloudDataValue dataValue(List<String> identifiers, String name, @Nullable String reference,
			@Nullable Map<String, ?> metadata) {
		return new CloudDataValue(identifiers, name, reference, metadata);
	}

	/**
	 * Create a new data value instance.
	 *
	 * @param identifiers the value identifiers, unique within the overall hierarchy
	 * @param name        the component name
	 * @param reference   the unique hierarchy reference
	 * @param metadata    the metadata
	 * @param children    the optional children values
	 * @throws IllegalArgumentException if {@code identifiers} or {@code name} is
	 *                                  {@code null}
	 */
	public static CloudDataValue dataValue(List<String> identifiers, String name, @Nullable String reference,
			@Nullable Map<String, ?> metadata, @Nullable Collection<CloudDataValue> children) {
		return new CloudDataValue(identifiers, name, reference, metadata, children);
	}

	/**
	 * Search for the first value with a given set of identifiers.
	 *
	 * @param values      the values to search
	 * @param identifiers the identifiers to search for
	 * @return the first matching value, or {@code null}
	 */
	public static @Nullable CloudDataValue findFirst(CloudDataValue[] values, List<String> identifiers) {
		for (int i = 0; i < values.length; i++) {
			var result = values[i].findFirst(identifiers);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Create a flat list out of a list of cloud data values.
	 * 
	 * <p>
	 * The children of each data value will be recursively added to the resulting
	 * list.
	 * </p>
	 * 
	 * @param dataValues the data values to flatten
	 * @return the flattened data values list
	 */
	public static List<CloudDataValue> flatList(List<CloudDataValue> dataValues) {
		List<CloudDataValue> result = new ArrayList<>(dataValues.size());
		for (CloudDataValue dataValue : dataValues) {
			addToFlatList(dataValue, result);
		}
		return result;
	}

	private static void addToFlatList(CloudDataValue dataValue, List<CloudDataValue> result) {
		result.add(dataValue);
		Collection<CloudDataValue> children = dataValue.getChildren();
		if (children != null) {
			for (CloudDataValue child : children) {
				addToFlatList(child, result);
			}
		}
	}

	/**
	 * Constructor.
	 *
	 * @param identifiers the value identifiers, unique within the overall hierarchy
	 * @param name        the component name
	 * @param reference   the unique hierarchy reference
	 * @param metadata    the metadata
	 * @throws IllegalArgumentException if {@code identifiers} or {@code name} is
	 *                                  {@code null}
	 */
	public CloudDataValue(List<String> identifiers, String name, @Nullable String reference,
			@Nullable Map<String, ?> metadata) {
		this(identifiers, name, reference, metadata, null);
	}

	/**
	 * Constructor.
	 *
	 * @param identifiers the value identifiers, unique within the overall hierarchy
	 * @param name        the component name
	 * @param reference   the unique hierarchy reference
	 * @param metadata    the metadata
	 * @param children    the optional children values
	 * @throws IllegalArgumentException if {@code identifiers} or {@code name} is
	 *                                  {@code null}
	 */
	public CloudDataValue(List<String> identifiers, String name, @Nullable String reference,
			@Nullable Map<String, ?> metadata, @Nullable Collection<CloudDataValue> children) {
		super();
		this.identifiers = requireNonNullArgument(identifiers, "identifiers");
		this.name = requireNonNullArgument(name, "name");
		this.reference = reference;
		this.metadata = metadata;
		this.children = children;
	}

	@Override
	public int compareTo(CloudDataValue o) {
		final int lenLeft = identifiers.size();
		final int lenRight = o.identifiers.size();
		for (int i = 0, len = Math.min(lenLeft, lenRight); i < len; i++) {
			int result = StringUtils.naturalSortCompare(identifiers.get(i), o.identifiers.get(i), true);
			if (result != 0) {
				return result;
			}
		}
		if (lenLeft < lenRight) {
			return -1;
		} else if (lenLeft > lenRight) {
			return 1;
		}
		return 0;
	}

	/**
	 * Search for the first value with a given set of identifiers.
	 *
	 * @param identifiers the identifiers to search for
	 * @return the first matching value, or {@code null}
	 * @since 1.9
	 */
	public @Nullable CloudDataValue findFirst(List<String> identifiers) {
		if (this.identifiers.equals(identifiers)) {
			return this;
		}
		if (this.identifiers.size() < identifiers.size()) {
			boolean prefixMatch = true;
			for (int i = 0, max = this.identifiers.size(); prefixMatch && i < max; i++) {
				if (!this.identifiers.get(i).equals(identifiers.get(i))) {
					prefixMatch = false;
				}
			}
			if (!prefixMatch) {
				// abort
				return null;
			}
		}
		Collection<CloudDataValue> children = getChildren();
		if (children != null) {
			for (CloudDataValue child : children) {
				var result = child.findFirst(identifiers);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		if (reference != null) {
			return reference;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("CloudDataValue{");
		if (identifiers != null) {
			builder.append("identifiers=");
			builder.append(identifiers);
			builder.append(", ");
		}
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if (metadata != null) {
			builder.append("metadata=");
			builder.append(metadata);
			builder.append(", ");
		}
		if (children != null) {
			builder.append("children=");
			builder.append(children);
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the data value hierarchy identifier.
	 *
	 * @return the identifiers, unique within the overall hierarchy, never
	 *         {@code null}
	 */
	public final List<String> getIdentifiers() {
		return identifiers;
	}

	/**
	 * Get the component name.
	 *
	 * @return the name, never {@code null}
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Get the reference.
	 *
	 * @return the reference, or {@code null}
	 */
	public final @Nullable String getReference() {
		return reference;
	}

	/**
	 * Get the component metadata.
	 *
	 * @return the metadata, or {@code null}
	 */
	public final @Nullable Map<String, ?> getMetadata() {
		return metadata;
	}

	/**
	 * Get the component children.
	 *
	 * @return the children
	 */
	public final @Nullable Collection<CloudDataValue> getChildren() {
		return children;
	}

}
