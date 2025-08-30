package s10k.tool.common.domain;

import java.time.Instant;
import java.util.Arrays;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.solarnetwork.security.Snws2AuthorizationBuilder;
import net.solarnetwork.web.jakarta.security.AuthorizationCredentialsProvider;
import net.solarnetwork.web.jakarta.support.StaticAuthorizationCredentialsProvider;

/**
 * SolarNetwork token credentials.
 */
@SuppressWarnings("ArrayRecordComponent")
@RegisterReflectionForBinding
public record SnTokenCredentials(
// @formatter:off
		@JsonProperty("sn_token_id")
		String tokenId,
		
		@JsonProperty("sn_token_secret")
		char[] tokenSecret
		// @formatter:on
) {

	@Override
	public String toString() {
		return "SnTokenCredentials[tokenId=" + tokenId + "]";
	}

	/**
	 * Get a credentials provider from these credentials.
	 * 
	 * <p>
	 * This method is designed to be called only once. When invoked, the token
	 * secret will be cleared from memory and thus no longer available.
	 * </p>
	 * 
	 * @param signingDate the signing date to use
	 * @return the provider
	 * @throws IllegalStateException if this method has already been called, or the
	 *                               token secret is not available
	 */
	public AuthorizationCredentialsProvider credentialsProvider(Instant signingDate) {
		if (tokenSecret == null || tokenSecret.length < 1 || tokenSecret[0] == '0') {
			throw new IllegalStateException("The token secret is not available.");
		}
		var result = new StaticAuthorizationCredentialsProvider(tokenId,
				new Snws2AuthorizationBuilder(tokenId).computeSigningKey(signingDate, String.valueOf(tokenSecret)),
				signingDate);

		// clear out the secret from memory
		Arrays.fill(tokenSecret, '0');

		return result;
	}

}
