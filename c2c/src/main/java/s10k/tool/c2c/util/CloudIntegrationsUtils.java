package s10k.tool.c2c.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
	public static String integrationServiceLocalizedName(String serviceId) {
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
	public static String datumStreamServiceLocalizedName(String serviceId) {
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
	 * @return the matching service ID and name, or {@code null} if not found
	 */
	public static Entry<String, String> findDatumStreamServiceId(String query) {
		if (query == null || query.isEmpty()) {
			return null;
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
		return null;
	}

}
