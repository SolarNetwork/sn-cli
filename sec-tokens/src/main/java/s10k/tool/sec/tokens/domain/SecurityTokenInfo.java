package s10k.tool.sec.tokens.domain;

import java.time.Instant;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import net.solarnetwork.domain.SecurityPolicy;

/**
 * Security token information.
 */
@RegisterReflectionForBinding
public record SecurityTokenInfo(String id, Instant created, Long userId, String name, String description, String status,
		String type, boolean expired, SecurityPolicy policy) {

}
