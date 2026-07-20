package s10k.tool.c2c.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jspecify.annotations.Nullable;

/**
 * Helper utilities for Cloud Integrations.
 */
public final class CloudIntegrationsUtils {

	private CloudIntegrationsUtils() {
		// not available
	}

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("s10k.tool.c2c.util.messages");

	/**
	 * Get a localized name for a Cloud Integration service.
	 * 
	 * @param serviceId the integration service ID
	 * @return the localized name, falling back to {@code serviceId} if no localized
	 *         name available
	 */
	public static @Nullable String integrationServiceLocalizedName(@Nullable String serviceId) {
		if (serviceId == null) {
			return null;
		}
		try {
			return RESOURCE_BUNDLE.getString("i9n." + serviceId + ".name");
		} catch (MissingResourceException e) {
			int lastDotIdx = serviceId.lastIndexOf('.');
			if (lastDotIdx >= 0 && lastDotIdx + 1 < serviceId.length()) {
				return serviceId.substring(lastDotIdx + 1);
			}
			return serviceId;
		}
	}

	/**
	 * Get a localized name for a Cloud Datum Stream service.
	 * 
	 * @param serviceId the datum stream service ID
	 * @return the localized name, falling back to {@code serviceId} if no localized
	 *         name available
	 */
	public static @Nullable String datumStreamServiceLocalizedName(@Nullable String serviceId) {
		if (serviceId == null) {
			return null;
		}
		try {
			return RESOURCE_BUNDLE.getString("ds." + serviceId + ".name");
		} catch (MissingResourceException e) {
			int lastDotIdx = serviceId.lastIndexOf('.');
			if (lastDotIdx >= 0 && lastDotIdx + 1 < serviceId.length()) {
				return serviceId.substring(lastDotIdx + 1);
			}
			return serviceId;
		}
	}

	/**
	 * Compare two periods.
	 * 
	 * @param l the first period
	 * @param r the second period
	 * @return {@code 0} if {@code l == r}; a value less than {@code 0} if
	 *         {@code l.isBefore(r)}; a value greater than {@code 0} if
	 *         {@code l.isAfter(r)}
	 */
	public static int comparePeriods(Period l, Period r) {
		return LocalDate.EPOCH.plus(l).compareTo(LocalDate.EPOCH.plus(r));
	}

	/**
	 * Lookup a Cloud Datum Stream service ID using a case-insensitive substring
	 * search.
	 * 
	 * @param query the substring to look for
	 * @return the matching service ID and name
	 * @throws IllegalStateException if a matching service ID is not found
	 */
	public static Entry<String, String> findDatumStreamServiceId(String query) {
		if (query == null || query.isEmpty()) {
			throw new IllegalStateException("Datum stream type not provided.");
		}
		final String lcQuery = query.toLowerCase(Locale.ENGLISH);
		for (String key : RESOURCE_BUNDLE.keySet()) {
			if (!(key.startsWith("ds.") && key.endsWith(".name"))) {
				continue;
			}
			final String val = RESOURCE_BUNDLE.getString(key);
			if (key.contains(lcQuery) || val.toLowerCase(Locale.getDefault()).contains(lcQuery)) {
				return Map.entry(key.substring(3, key.length() - 5), val);
			}
		}
		throw new IllegalStateException("Datum stream type not found for [" + query + "]");
	}

	private static List<String> findBundleKeys(final String prefix, final String suffix, final String[] queries) {
		if (queries == null || queries.length < 0) {
			return List.of();
		}
		final List<String> result = new ArrayList<>(queries.length);
		for (String query : queries) {
			final String lcQuery = query.toLowerCase(Locale.ENGLISH);
			for (String key : RESOURCE_BUNDLE.keySet()) {
				if (!(key.startsWith(prefix) && key.endsWith(suffix))) {
					continue;
				}
				final String val = RESOURCE_BUNDLE.getString(key);
				if (key.contains(lcQuery) || val.toLowerCase(Locale.getDefault()).contains(lcQuery)) {
					result.add(key.substring(prefix.length(), key.length() - suffix.length()));
				}
			}
		}
		return result;
	}

	/**
	 * Lookup a Cloud Integration service IDs using a case-insensitive substring
	 * search.
	 * 
	 * @param queries the substrings to look for
	 * @return the list of matching service IDs, never {@code null}
	 */
	public static List<String> findIntegrationServiceIds(final String[] queries) {
		return findBundleKeys("i9n.", ".name", queries);
	}

	/**
	 * Lookup a Cloud Datum Stream service IDs using a case-insensitive substring
	 * search.
	 * 
	 * @param queries the substrings to look for
	 * @return the matching service ID and name
	 * @return the list of matching service IDs, never {@code null}
	 */
	public static List<String> findDatumStreamServiceIds(final String[] queries) {
		return findBundleKeys("ds.", ".name", queries);
	}

}
