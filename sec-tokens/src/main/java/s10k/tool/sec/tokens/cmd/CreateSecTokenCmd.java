package s10k.tool.sec.tokens.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.Collections;
import java.util.List;
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
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.SecurityPolicy;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.sec.tokens.domain.SecurityTokenInfo;
import s10k.tool.sec.tokens.domain.SecurityTokenType;

/**
 * Create a new security token.
 */
@Component
@Command(name = "create", sortSynopsis = false)
public class CreateSecTokenCmd extends BaseSubCmd<SecTokensCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-t", "--type" },
			description = "the type of token",
			required =  true)
	SecurityTokenType tokenType;
	
	@Option(names = { "-n", "--name" },
			description = "a name to give the token")
	String name;
	
	@Option(names = { "-D", "--description" },
			description = "a description to give the token")
	String description;
	
	@Mixin
	SecurityPolicyOptions policyOptions;

	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public CreateSecTokenCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final SecurityPolicy policy = (policyOptions != null ? policyOptions.toPolicy() : null);
		final RestClient restClient = restClient();
		final ObjectWriter pretty = objectMapper.writerWithDefaultPrettyPrinter();

		try {
			SecurityTokenInfo result = createSecurityToken(restClient, objectMapper, tokenType, name, description,
					policy);
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
	 * Create a new security token.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the mapper to use
	 * @param type         the token type
	 * @param name         an optional name
	 * @param description  an optional description
	 * @param policy       an optional policy
	 * @throws IllegalStateException if the token cannot be created
	 */
	public static SecurityTokenInfo createSecurityToken(RestClient restClient, ObjectMapper objectMapper,
			SecurityTokenType type, String name, String description, SecurityPolicy policy) {
		assert type != null;

		// @formatter:off
		var createReq = restClient.post().uri(b -> {
			return b.path("/solaruser/api/v1/sec/user/auth-tokens/generate/{type}")
					.build(type.name());
		});
		if (policy != null) {
			createReq.contentType(MediaType.APPLICATION_JSON)
				.body(policy)
				;
		}
		final JsonNode response = createReq
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
				;
		// @formatter:on

		checkSuccess(response);

		SecurityTokenInfo info;
		try {
			info = objectMapper.treeToValue(response.path("data"), SecurityTokenInfo.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing security token response: " + e.getMessage(), e);
		}

		final var infoBody = new LinkedMultiValueMap<>(2);
		if (name != null && !name.isBlank()) {
			infoBody.add("name", name);
		}
		if (description != null && !description.isBlank()) {
			infoBody.add("description", description);
		}
		if (!infoBody.isEmpty()) {
			infoBody.add("tokenId", info.id());
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

			info = info.copyWithInfo(name, description);
		}

		return info;
	}

	/**
	 * Get token info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] tokenColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Token ID").dataAlign(LEFT),
				new Column().header("Token Secret").dataAlign(LEFT),
				new Column().header("Created").dataAlign(LEFT),
				new Column().header("User ID").dataAlign(RIGHT),
				new Column().header("Type").dataAlign(LEFT),
				new Column().header("Name").dataAlign(LEFT),
				new Column().header("Description").dataAlign(LEFT),
				new Column().header("Policy").dataAlign(LEFT),	
			};
		// @formatter:on
	}

	/**
	 * Convert token info into a tabular structure.
	 * 
	 * @param m the metadata to convert
	 * @return the metadata data
	 */
	public static Object[] tokenRow(SecurityTokenInfo m, ObjectWriter policyWriter) {
		String policy = null;
		try {
			policy = m.policy() != null ? policyWriter.writeValueAsString(m.policy()) : null;
		} catch (JsonProcessingException e) {
			// ignore and continue
		}
		// @formatter:off
		return new Object[] {
				m.id(),
				m.authSecret(),
				m.created(),
				m.userId(),
				m.type(),
				m.name(),
				m.description(),
				policy
			};
		// @formatter:on
	}

}
