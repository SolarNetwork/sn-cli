package s10k.tool.sec.tokens.cmd;

import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.sec.tokens.cmd.ListSecTokensCmd.listSecurityTokens;
import static s10k.tool.sec.tokens.cmd.ListSecTokensCmd.tokenColumns;
import static s10k.tool.sec.tokens.cmd.ListSecTokensCmd.tokenRow;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.solarnetwork.domain.SecurityPolicy;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.sec.tokens.domain.SecurityTokenFilter;
import s10k.tool.sec.tokens.domain.SecurityTokenInfo;

/**
 * Update security token info.
 */
@Component
@Command(name = "update", sortSynopsis = false)
public class UpdateSecTokenCmd extends BaseSubCmd<SecTokensCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-id", "--identifier" },
			description = "the ID of the token to update",
			paramLabel = "tokenId",
			required =  true)
	String identifier;

	@Option(names = { "-n", "--name" },
			description = "a name to give the token")
	String name;
	
	@Option(names = { "-D", "--description" },
			description = "a description to give the token")
	String description;
	
	@ArgGroup(exclusive = true, multiplicity = "0..1")
	ActiveOrDisabled activeOrDisabled;
	
	@Mixin
	SecurityPolicyOptions policyOptions;
	
	@Option(names = { "-R", "--replace" },
			description = "replace the security policy, otherwise update policy")
	boolean replacePolicy;

	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Grouping of active/disabled mode flags.
	 */
	static class ActiveOrDisabled {
		// @formatter:off
		@Option(names = {"-a", "--active"},
				description = "match only active tokens")
		boolean active;
		
		@Option(names = {"-d", "--disabled"},
				description = "match only disabled tokens")
		boolean disabled;
    	// @formatter:on

	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public UpdateSecTokenCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final SecurityPolicy policy = (policyOptions != null ? policyOptions.toPolicy() : null);
		final RestClient restClient = restClient();
		final ObjectWriter pretty = objectMapper.writerWithDefaultPrettyPrinter();

		if ((name == null || name.isBlank()) && (description == null || description.isBlank())
				&& activeOrDisabled == null && policy == null) {
			System.err.println("No update options provided.");
			return 3;
		}

		try {
			SecurityTokenInfo result = updateSecurityToken(restClient, objectMapper, identifier, name, description,
					(activeOrDisabled != null
							? (activeOrDisabled.active ? "Active" : activeOrDisabled.disabled ? "Disabled" : null)
							: null),
					policy, replacePolicy);
			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? Collections.singletonList(result)
					: Collections.singletonList(tokenRow(result, pretty)));
			TableUtils.renderTableData(tokenColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error creating security token: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Update a security token.
	 * 
	 * @param restClient    the REST client
	 * @param objectMapper  the mapper to use
	 * @param identifier    the token ID to update
	 * @param name          an optional name
	 * @param description   an optional description
	 * @param status        an optional status to update the token to
	 *                      ({@code Active} or {@code Disabled})
	 * @param policy        an optional policy
	 * @param replacePolicy {@code true} to replace, rather than update, the
	 *                      security policy
	 * @throws IllegalStateException if the token cannot be created
	 */
	public static SecurityTokenInfo updateSecurityToken(RestClient restClient, ObjectMapper objectMapper,
			String identifier, String name, String description, String status, SecurityPolicy policy,
			boolean replacePolicy) {
		final var infoBody = new LinkedMultiValueMap<>(2);
		if (name != null && !name.isBlank()) {
			infoBody.add("name", name);
		}
		if (description != null && !description.isBlank()) {
			infoBody.add("description", description);
		}
		if (!infoBody.isEmpty()) {
			infoBody.add("tokenId", identifier);
			// @formatter:off
			final JsonNode infoResponse = restClient.post()
					.uri("/solaruser/api/v1/sec/user/auth-tokens/info")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(infoBody)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class)
					;
			// @formatter:on

			checkSuccess(infoResponse);
		}

		if (status != null) {
			final var statusBody = new LinkedMultiValueMap<>(2);
			statusBody.add("tokenId", identifier);
			statusBody.add("status", status);
			// @formatter:off
			final JsonNode infoResponse = restClient.post()
					.uri("/solaruser/api/v1/sec/user/auth-tokens/status")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(statusBody)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class)
					;
			// @formatter:on

			checkSuccess(infoResponse);
		}

		if (policy != null) {
			// @formatter:off
			final JsonNode policyResponse = (replacePolicy ? restClient.put() : restClient.patch())
					.uri(b -> {
						return b.path("/solaruser/api/v1/sec/user/auth-tokens/policy")
								.queryParam("tokenId", identifier)
								.build();
					})
					.contentType(MediaType.APPLICATION_JSON)
					.body(policy)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class)
					;
			// @formatter:on

			checkSuccess(policyResponse);

			try {
				return objectMapper.treeToValue(policyResponse.path("data"), SecurityTokenInfo.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new IllegalStateException("Error parsing security token response: " + e.getMessage(), e);
			}
		}

		// fetch the token to get all updated properties
		var filter = new SecurityTokenFilter(null, Set.of(identifier), null);
		List<SecurityTokenInfo> results = listSecurityTokens(restClient, objectMapper, filter);
		return results.getFirst();
	}

}
