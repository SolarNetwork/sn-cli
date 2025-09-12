package s10k.tool.flux.cmd;

import static java.util.Map.entry;
import static net.solarnetwork.util.NumberUtils.bigDecimalForNumber;
import static net.solarnetwork.util.NumberUtils.narrow;
import static net.solarnetwork.util.NumberUtils.round;
import static s10k.tool.common.domain.ResultDisplayMode.JSON;
import static s10k.tool.common.domain.ResultDisplayMode.PRETTY;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.common.mqtt.BaseMqttConnectionService;
import net.solarnetwork.common.mqtt.BasicMqttProperty;
import net.solarnetwork.common.mqtt.MqttConnection;
import net.solarnetwork.common.mqtt.MqttConnectionFactory;
import net.solarnetwork.common.mqtt.MqttConnectionObserver;
import net.solarnetwork.common.mqtt.MqttMessage;
import net.solarnetwork.common.mqtt.MqttMessageHandler;
import net.solarnetwork.common.mqtt.MqttPropertyType;
import net.solarnetwork.common.mqtt.MqttQos;
import net.solarnetwork.common.mqtt.MqttVersion;
import net.solarnetwork.common.mqtt.netty.NettyMqttConnectionFactory;
import net.solarnetwork.util.StatTracker;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ProfileInfo;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * Subscribe to a SolarFlux topic and display the results in real-time like the
 * Unix {@code tail} utility.
 */
@Component
@Command(name = "tail", sortSynopsis = false)
public class TailFluxCmd extends BaseSubCmd<FluxCmd> implements Callable<Integer> {

	// @formatter:off
	@ArgGroup(exclusive = true, multiplicity = "1")
	TopicOrNodeSource topicOrNodeSource;

	@Option(names = { "-prop", "--property" },
			description = "show only this property name in the results",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "propName")
	String[] propertyNames;
	
	@Option(names = { "-G", "--csv-global-header" },
			description = "for CSV output, generate a global header from the -prop name(s) or first result")
	boolean csvGlobalHeader;
	
	@Option(names = {"-R", "--max-precision" },
			description = "max number precision, or -1 for no rounding",
			defaultValue = "3")
	int maxPrecision = 3;
	
	@Option(names = { "--client-id" },
			description = "a client ID to use, instead of a random default")
	String clientIdSuffix = "_" +UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
	
	@Option(names = {"--url"},
			description = "the MQTT server URL to connect to, in @|bold mqtt(s)://host:port|@ form",
			defaultValue = "mqtts://fluxion.solarnetwork.net:8885",
			paramLabel = "url")
	String serverUri = "mqtts://fluxion.solarnetwork.net:8885";
	
	@Option(names = "--mqtt-version",
			description = "the MQTT version to use")
	MqttVersion mqttVersion;

	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the results",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Grouping of node/source IDs, where both must be provided.
	 */
	static class NodeAndSourceIds {
		// @formatter:off
    	@Option(names = { "-node", "--node-id" },
    			description = "a node ID (or + wildcard) to subscribe to")
    	String nodeId;

    	@Option(names = { "-source", "--source-id" },
    			description = "a source ID (or topic pattern) to subscribe to")
    	String sourceId;
    	// @formatter:on

	}

	/**
	 * Grouping of topic or node/source, where one or the other must be provided.
	 */
	static class TopicOrNodeSource {
		// @formatter:off
    	@Option(names = { "-t", "--topic" },
    			description = "a topic filter to subscribe to",
    			paramLabel = "filter")
    	String topicFilter;

    	@ArgGroup(exclusive = false)
    	NodeAndSourceIds nodeAndSource;
    	// @formatter:on

		/**
		 * Test if node and source IDs are provided, otherwise the topic filter should
		 * be used.
		 * 
		 * @return {@code true} if node and source IDs are provided
		 */
		boolean isNodeAndSource() {
			return nodeAndSource != null && nodeAndSource.nodeId != null && !nodeAndSource.nodeId.isBlank()
					&& nodeAndSource.sourceId != null && !nodeAndSource.sourceId.isBlank();
		}

		/**
		 * Test if node and source IDs are <b>not</b> provided, and the topic filter has
		 * a {@code user/} prefix.
		 * 
		 * @return {@code true} if a user topic prefix is specified
		 */
		boolean isUserTopic() {
			return !isNodeAndSource() && topicFilter != null && topicFilter.startsWith("user/");
		}
	}

	private final ObjectMapper cborObjectMapper;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public TailFluxCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
		this.cborObjectMapper = objectMapper.copyWith(new CBORFactory());
	}

	@Override
	public Integer call() throws Exception {
		// bug in SolarFlux MQTTv5 where topic rewriting of "node/X" to "user/U/node/X"
		// prevents delivery of messages
		if (mqttVersion != null && mqttVersion.compareTo(MqttVersion.Mqtt5) >= 0) {
			StringBuilder buf = new StringBuilder();
			if (topicOrNodeSource.isNodeAndSource()) {
				mqttVersion = MqttVersion.Mqtt311;
				buf.append("Switching to MQTT v3.1.1; use --topic option for MQTTv5 support.\nUse a topic style like ");
			} else if (!topicOrNodeSource.isUserTopic()) {
				mqttVersion = MqttVersion.Mqtt311;
				buf.append("Switching to MQTT v3.1.1; for MQTTv5 support use a topic style like ");
			}
			if (!buf.isEmpty()) {
				buf.append(
						"@|bold user/|@@|bold,yellow USER_ID|@@|bold /node/|@@|bold,yellow NODE_ID|@@|bold /datum/0/|@@|bold,yellow SOURCE_ID|@.");
				System.err.println(Ansi.AUTO.string(buf.toString()));
			}
		} else if (mqttVersion == null) {
			mqttVersion = topicOrNodeSource.isUserTopic() ? MqttVersion.Mqtt5 : MqttVersion.Mqtt311;
		}

		final ProfileInfo profile = profileWithCredentials();

		try (final var executor = Executors.newCachedThreadPool();
				final var scheduler = Executors.newSingleThreadScheduledExecutor()) {

			final StatTracker stats = new StatTracker("SolarFlux", null, null, 1000);

			final var connectionFactory = new NettyMqttConnectionFactory(executor,
					new ConcurrentTaskScheduler(executor, scheduler));

			final CompletableFuture<Object> closeFuture = new CompletableFuture<Object>();
			final var client = new MqttConnectionService(connectionFactory, stats, closeFuture);
			final var mqttConfig = client.getMqttConfig();
			mqttConfig.setCleanSession(true);
			mqttConfig.setReconnect(false);
			mqttConfig.setServerUriValue(serverUri);
			mqttConfig.setUsername(profile.tokenCredentials().tokenId());
			mqttConfig.setPassword(String.valueOf(profile.tokenCredentials().tokenSecret()));
			mqttConfig.setClientId(profile.tokenCredentials().tokenId() + clientIdSuffix);
			mqttConfig.setReadTimeoutSeconds(0); // wait forever for messages
			mqttConfig.setWriteTimeoutSeconds(55); // we never write, so send PING every min
			mqttConfig.setReconnectDelaySeconds(1);
			mqttConfig.setVersion(mqttVersion != null ? mqttVersion : MqttVersion.Mqtt311);
			mqttConfig.setWireLoggingEnabled(true);
			if (mqttVersion.compareTo(MqttVersion.Mqtt5) >= 0) {
				mqttConfig.setProperty(new BasicMqttProperty<>(MqttPropertyType.TOPIC_ALIAS_MAXIMUM, 255));
			}
			Future<?> startFuture = client.startup();
			startFuture.get();
			closeFuture.get();

			return 0;
		} catch (Exception e) {
			System.err.println("Error streaming datum: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private String mqttTopicFilter() {
		assert topicOrNodeSource != null;
		if (topicOrNodeSource.isNodeAndSource()) {
			return "node/%s/datum/0/%s".formatted(topicOrNodeSource.nodeAndSource.nodeId,
					topicOrNodeSource.nodeAndSource.sourceId);
		}
		return topicOrNodeSource.topicFilter;
	}

	private final class MqttConnectionService extends BaseMqttConnectionService
			implements MqttConnectionObserver, MqttMessageHandler {

		private final CompletableFuture<Object> future;
		private final ObjectWriter jsonOut;
		private final OutputStream out;

		private AtomicLong count = new AtomicLong();
		private List<Column> globalColumns;

		private MqttConnectionService(MqttConnectionFactory connectionFactory, StatTracker mqttStats,
				CompletableFuture<Object> future) {
			super(connectionFactory, mqttStats);
			this.future = future;
			jsonOut = objectMapper.writerWithDefaultPrettyPrinter();
			out = StreamUtils.nonClosing(System.out);
		}

		@Override
		public String getPingTestName() {
			return "N/A";
		}

		@Override
		public void onMqttMessage(MqttMessage message) {
			final long msgNum = count.incrementAndGet();
			try {
				Map<String, Object> msgBody = JsonUtils
						.getStringMapFromTree(cborObjectMapper.readTree(message.getPayload()));
				List<Column> cols = msgColumns(msgBody.keySet());
				Map<String, Object> displayMap = new LinkedHashMap<>(msgBody.size());
				for (Column col : cols) {
					displayMap.put(col.getHeader(), propVal(col.getHeader(), msgBody.get(col.getHeader())));
				}
				if (displayMode == JSON) {
					// @formatter:off
					jsonOut.writeValue(out, Map.ofEntries(
							entry("ts", Instant.now()),
							entry("topic", message.getTopic()),
							entry("body", displayMap)
							));
					System.out.println();
					// @formatter:on
				} else {
					// @formatter:off
					TableUtils.renderTableData(displayMode == PRETTY || !csvGlobalHeader || msgNum == 1
								? cols.toArray(Column[]::new)
								: null,
							List.of(displayMap.values()), displayMode, objectMapper, out);
					// @formatter:on
				}
			} catch (IOException e) {
				System.err.println("Error decoding MQTT message: " + e.toString());
			}
		}

		private Object propVal(final String propName, final Object val) {
			Object result = val;
			if (val instanceof Number n) {
				if (propName.equals("created")) {
					result = Instant.ofEpochMilli(n.longValue());
				} else if (maxPrecision >= 0) {
					result = bigDecimalForNumber(narrow(round(n, maxPrecision), 2)).toPlainString();
				}
			}
			return result;
		}

		private synchronized List<Column> msgColumns(Set<String> msgKeys) {
			// check if global columns are required
			if (globalColumns != null) {
				return globalColumns;
			}
			List<Column> result = new ArrayList<>(propertyNames != null ? propertyNames.length : msgKeys.size());
			if (propertyNames != null) {
				for (String propName : propertyNames) {
					result.add(new Column().header(propName).dataAlign(HorizontalAlign.RIGHT));
				}
			} else {
				for (String msgKey : msgKeys) {
					if (msgKey.equals("_v")) {
						// skip the v2 flag
						continue;
					}
					Column col = new Column().header(msgKey).dataAlign(HorizontalAlign.RIGHT);
					if (msgKey.equals("created")) {
						result.add(0, col);
					} else {
						result.add(col);
					}
				}
			}
			if (displayMode == ResultDisplayMode.CSV && csvGlobalHeader) {
				globalColumns = result;
			}
			return result;
		}

		@Override
		public void onMqttServerConnectionLost(MqttConnection connection, boolean willReconnect, Throwable cause) {
			future.complete(null);
		}

		@SuppressWarnings("FutureReturnValueIgnored")
		@Override
		public void onMqttServerConnectionEstablished(MqttConnection connection, boolean reconnected) {
			connection.subscribe(mqttTopicFilter(), MqttQos.AtMostOnce, null);
		}

	}

}
