package s10k.tool.datum.imp.util;

import java.util.List;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jspecify.annotations.Nullable;

import s10k.tool.common.util.StringUtils;

/**
 * Datum import utilities.
 */
public final class DatumImportUtils {

	/** The Cloud Integrations datum input service ID. */
	public static final String CLOUD_INTEGRATIONS_INPUT_SERVICE_ID = "s10k.c2c.ds-import";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("s10k.tool.datum.imp.util.messages");

	/**
	 * 
	 */
	private DatumImportUtils() {
		// not available
	}

	/**
	 * Get an appropriate batch size for a given input service.
	 * 
	 * @param inputServiceId the input service ID
	 * @param batchSize      the given batch size
	 * @return if the service is for Cloud Integrations, then {@code 1} will always
	 *         be returned, otherwise {@code batchSize} will be returned as-is
	 */
	public static @Nullable Integer importBatchSize(final String inputServiceId, @Nullable Integer batchSize) {
		if (CLOUD_INTEGRATIONS_INPUT_SERVICE_ID.equals(inputServiceId)) {
			return 1;
		}
		return batchSize;
	}

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

	/**
	 * Lookup a Datum Import service ID using a case-insensitive substring search.
	 * 
	 * @param query the substring to look for
	 * @return the matching service ID and name
	 * @throws IllegalStateException if a matching service ID is not found
	 */
	public static Entry<String, String> findDatumImportServiceId(String query) {
		try {
			return StringUtils.findBundleEntry(RESOURCE_BUNDLE, "imp.", ".name", query);
		} catch (Exception e) {
			throw new IllegalStateException("Datum import type not found for [" + query + "]");
		}
	}

}
