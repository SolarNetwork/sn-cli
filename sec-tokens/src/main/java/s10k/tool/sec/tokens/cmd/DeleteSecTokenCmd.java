package s10k.tool.sec.tokens.cmd;

import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Delete a security token.
 */
@Component
@Command(name = "delete", sortSynopsis = false)
public class DeleteSecTokenCmd extends BaseSubCmd<SecTokensCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-id", "--identifier" },
			description = "the ID of the token to delete",
			paramLabel = "tokenId",
			required =  true)
	String identifier;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public DeleteSecTokenCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		try {
			// @formatter:off
			final JsonNode response = restClient.delete()
					.uri(b -> {
						return b.path("/solaruser/api/v1/sec/user/auth-tokens")
								.queryParam("tokenId", identifier)
								.build();
					})
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class)
					;
			// @formatter:on

			checkSuccess(response);

			System.out.println("Security token deleted.");

			return 0;
		} catch (Exception e) {
			System.err.println("Error deleting security token: %s".formatted(e.getMessage()));
		}
		return 1;
	}

}
