package s10k.tool.common.util;

import java.time.LocalDateTime;

import picocli.CommandLine.ITypeConverter;

/**
 * Custom converter for {@link LocalDateTime}.
 */
public class LocalDateTimeConverter implements ITypeConverter<LocalDateTime> {

	/**
	 * Constructor.
	 */
	public LocalDateTimeConverter() {
		super();
	}

	@Override
	public LocalDateTime convert(String value) throws Exception {
		return StringUtils.parseLocalDateTime(value);
	}

}
