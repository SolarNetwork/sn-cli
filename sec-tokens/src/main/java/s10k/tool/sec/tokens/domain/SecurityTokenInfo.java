package s10k.tool.sec.tokens.domain;

import java.time.Instant;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import net.solarnetwork.domain.SecurityPolicy;

/**
 * Security token information.
 */
@RegisterReflectionForBinding
public record SecurityTokenInfo(String id, String authSecret, Instant created, Long userId, String name,
		String description, String status, String type, boolean expired, SecurityPolicy policy) {

	/**
	 * Create a copy with a new name and description.
	 * 
	 * @param name        the name to use
	 * @param description the description to use
	 * @return the new instance
	 */
	public SecurityTokenInfo copyWithInfo(String name, String description) {
		return new SecurityTokenInfo(id, authSecret, created, userId, name, description, status, type, expired, policy);
	}

}
