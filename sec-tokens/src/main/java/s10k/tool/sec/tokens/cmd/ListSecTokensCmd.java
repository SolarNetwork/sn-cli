package s10k.tool.sec.tokens.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.RestUtils.populateQueryParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.sec.tokens.domain.SecurityTokenFilter;
import s10k.tool.sec.tokens.domain.SecurityTokenInfo;

/**
 * List security tokens.
 */
@Component
@Command(name = "list", sortSynopsis = false)
public class ListSecTokensCmd extends BaseSubCmd<SecTokensCmd> implements Callable<Integer> {

	// @formatter:off
	@ArgGroup(exclusive = true, multiplicity = "0..1")
	ActiveOrDisabled activeOrDisabled;
	
	@Option(names = { "-id", "--identifier" },
			description = "an identifier to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "tokenId")
	String[] identifiers;
	
	@Option(names = { "-t", "--type" },
			description = "the type of token")
	TokenType tokenType;
	
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

	/** Token type enumeration. */
	enum TokenType {
		/** Read node data. */
		ReadNodeData,

		/** User. */
		User,

		;
	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListSecTokensCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final SecurityTokenFilter filter = SecurityTokenFilter.securityTokenFilter(
				activeOrDisabled != null ? activeOrDisabled.active : null, identifiers,
				(tokenType != null ? new String[] { tokenType.name() } : null));

		final RestClient restClient = restClient();

		final ObjectWriter pretty = objectMapper.writerWithDefaultPrettyPrinter();

		try {
			List<SecurityTokenInfo> infos = listSecurityTokens(restClient, objectMapper, filter);
			if (infos.isEmpty()) {
				System.err.println("No security tokens matched your criteria.");
				return 0;
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? infos
					: infos.stream().map(info -> metadataRow(info, pretty)).toList());
			// @formatter:off
			TableUtils.renderTableData(new Column[] {
					new Column().header("Token ID").dataAlign(LEFT),
					new Column().header("Created").dataAlign(LEFT),
					new Column().header("User ID").dataAlign(RIGHT),
					new Column().header("Type").dataAlign(LEFT),
					new Column().header("Status").dataAlign(LEFT),
					new Column().header("Name").dataAlign(LEFT),
					new Column().header("Description").dataAlign(LEFT),
					new Column().header("Policy").dataAlign(LEFT),	
				}, tableData, displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			// @formatter:on
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing security tokens: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Convert datum stream metadata into a tabular structure.
	 * 
	 * @param m the metadata to convert
	 * @return the metadata data
	 */
	public static Object[] metadataRow(SecurityTokenInfo m, ObjectWriter policyWriter) {
		String policy = null;
		try {
			policy = m.policy() != null ? policyWriter.writeValueAsString(m.policy()) : null;
		} catch (JsonProcessingException e) {
			// ignore and continue
		}
		// @formatter:off
		return new Object[] {
				m.id(),
				m.created(),
				m.userId(),
				m.type(),
				m.status(),
				m.name(),
				m.description(),
				policy
			};
		// @formatter:on
	}

	/**
	 * List stream metadata.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @return the tokens
	 * @throws IllegalStateException if the stream metadata is not available
	 */
	public static List<SecurityTokenInfo> listSecurityTokens(RestClient restClient, ObjectMapper objectMapper,
			SecurityTokenFilter filter) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/auth-tokens/find");
				populateQueryParameters(b, () -> filter.toRequestMap());
				return b.build();
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		final JsonNode resultsNode = response.path("data").path("results");

		List<SecurityTokenInfo> result = new ArrayList<>(resultsNode.size());
		for (JsonNode node : resultsNode) {
			SecurityTokenInfo info;
			try {
				info = objectMapper.treeToValue(node, SecurityTokenInfo.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new IllegalStateException("Error parsing security tokens response: " + e.getMessage(), e);
			}
			if (info != null) {
				result.add(info);
			}
		}
		return result;

	}

}
