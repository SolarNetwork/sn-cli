package s10k.tool.c2c.util;

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

}
