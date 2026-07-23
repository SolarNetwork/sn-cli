package s10k.tool.datum.util;

import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jspecify.annotations.Nullable;

import s10k.tool.common.util.StringUtils;

/**
 * Helper utilities for Datum.
 */
public final class DatumUtils {

	private DatumUtils() {
		// not available
	}

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("s10k.tool.datum.util.messages");

	/**
	 * Get a localized name for a Datum Import service.
	 * 
	 * @param serviceId the datum import service ID
	 * @return the localized name, falling back to {@code serviceId} if no localized
	 *         name available
	 */
	public static @Nullable String datumImportServiceLocalizedName(@Nullable String serviceId) {
		if (serviceId == null) {
			return null;
		}
		try {
			return RESOURCE_BUNDLE.getString("imp." + serviceId + ".name");
		} catch (MissingResourceException e) {
			int lastDotIdx = serviceId.lastIndexOf('.');
			if (lastDotIdx >= 0 && lastDotIdx + 1 < serviceId.length()) {
				return serviceId.substring(lastDotIdx + 1);
			}
			return serviceId;
		}
	}

	/**
	 * Lookup a Datum Import service IDs using a case-insensitive substring search.
	 * 
	 * @param queries the substrings to look for
	 * @return the list of matching service IDs, never {@code null}
	 */
	public static List<String> findDatumImportServiceIds(final String[] queries) {
		return StringUtils.findBundleKeys(RESOURCE_BUNDLE, "imp.", ".name", queries);
	}

}
