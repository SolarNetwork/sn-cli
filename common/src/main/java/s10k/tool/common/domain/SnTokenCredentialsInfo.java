package s10k.tool.common.domain;

/**
 * SolarNetwork token credentials information.
 * 
 * @param token     the token ID
 * @param tokenType the type of token, for example {@code User} or
 *                  {@code ReadNodeData}
 * @param userId    the account ID that owns the token
 */
public record SnTokenCredentialsInfo(String token, String tokenType, Long userId) {

}
