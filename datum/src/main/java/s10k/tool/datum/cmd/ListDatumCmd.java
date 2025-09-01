package s10k.tool.datum.cmd;

import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * View stream information.
 */
@Component
@Command(name = "list")
public class ListDatumCmd extends BaseSubCmd<DatumCmd> implements Callable<Integer> {

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListDatumCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
