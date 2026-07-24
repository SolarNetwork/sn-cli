package s10k.tool.common.util;

import org.springframework.util.MimeType;

/**
 * Encoding helper utilities.
 */
public final class EncodingUtils {

	private EncodingUtils() {
		// not available
	}

	/** The {@literal text/csv} MIME type. */
	public static final MimeType TEXT_CSV_MIME_TYPE = MimeType.valueOf("text/csv");

}
