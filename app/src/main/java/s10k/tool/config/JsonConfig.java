package s10k.tool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import s10k.tool.common.codec.GeneralDatumMetadataDeserializer;
import s10k.tool.common.codec.GeneralDatumMetadataSerializer;
import s10k.tool.instructions.codec.InstructionRequestSerializer;
import s10k.tool.instructions.domain.InstructionRequest;
import s10k.tool.nodes.codec.NodeCertificateInfoDeserializer;
import s10k.tool.nodes.codec.NodeInfoDeserializer;
import s10k.tool.nodes.codec.NodeMetadataDeserializer;
import s10k.tool.nodes.domain.NodeCertificateInfo;
import s10k.tool.nodes.domain.NodeInfo;
import s10k.tool.nodes.domain.NodeMetadata;

/**
 * JSON configuration.
 */
@Configuration(proxyBeanMethods = false)
public class JsonConfig {

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = JsonUtils.newDatumObjectMapper();

		SimpleModule toolModule = new SimpleModule("s10k");
		toolModule.addDeserializer(GeneralDatumMetadata.class, GeneralDatumMetadataDeserializer.INSTANCE);
		toolModule.addDeserializer(NodeCertificateInfo.class, NodeCertificateInfoDeserializer.INSTANCE);
		toolModule.addDeserializer(NodeInfo.class, NodeInfoDeserializer.INSTANCE);
		toolModule.addDeserializer(NodeMetadata.class, NodeMetadataDeserializer.INSTANCE);

		toolModule.addSerializer(GeneralDatumMetadata.class, GeneralDatumMetadataSerializer.INSTANCE);
		toolModule.addSerializer(InstructionRequest.class, InstructionRequestSerializer.INSTANCE);

		mapper.registerModule(toolModule);

		return mapper;
	}

	@Bean
	public MappingJackson2HttpMessageConverter objectMapperConverter(ObjectMapper mapper) {
		return new MappingJackson2HttpMessageConverter(mapper);
	}

}
