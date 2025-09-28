package s10k.tool.sec.tokens.domain;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Search filter for security tokens.
 * 
 * @param active      the active state, or {@code null}
 * @param identifiers the identifiers, or {@code null}
 * @param tokenTypes  the token types, or {@code null}
 */
public record SecurityTokenFilter(Boolean active, Collection<String> identifiers, Collection<String> tokenTypes) {

	/**
	 * Create a filter from array parameters.
	 * 
	 * @param active      the active state, or {@code null}
	 * @param identifiers the identifiers, or {@code null}
	 * @param tokenTypes  the token types, or {@code null}
	 * @return the new filter instance
	 */
	public static SecurityTokenFilter securityTokenFilter(Boolean active, String[] identifiers, String[] tokenTypes) {
		// @formatter:off
		return new SecurityTokenFilter(
				active,
				identifiers != null && identifiers.length > 0 ? Arrays.asList(identifiers) : null,
				tokenTypes != null && tokenTypes.length > 0 ? Arrays.asList(tokenTypes) : null
				);
		// @formatter:on
	}

	/**
	 * Get a multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters
	 */
	public MultiValueMap<String, Object> toRequestMap() {
		var postBody = new LinkedMultiValueMap<String, Object>(4);
		if (active != null) {
			postBody.set("active", active);
		}
		if (identifiers != null && !identifiers.isEmpty()) {
			postBody.set("identifiers", commaDelimitedStringFromCollection(identifiers));
		}
		if (tokenTypes != null && !tokenTypes.isEmpty()) {
			postBody.set("tokenTypes", commaDelimitedStringFromCollection(tokenTypes));
		}
		return postBody;
	}

}
