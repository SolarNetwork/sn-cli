package s10k.tool.common.domain;

import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * A service configuration.
 */
public class ServiceConfiguration {

	private @Nullable String name;
	private @Nullable String serviceIdentifier;
	private @Nullable Map<String, Object> serviceProperties;

	/**
	 * Get the name.
	 * 
	 * @return the name
	 */
	public final @Nullable String getName() {
		return name;
	}

	/**
	 * Set the name.
	 * 
	 * @param name the name to set
	 */
	public final void setName(@Nullable String name) {
		this.name = name;
	}

	/**
	 * Get the service identifier.
	 * 
	 * @return the serviceIdentifier
	 */
	public final @Nullable String getServiceIdentifier() {
		return serviceIdentifier;
	}

	/**
	 * Set the service identifier.
	 * 
	 * @param serviceIdentifier the serviceIdentifier to set
	 */
	public final void setServiceIdentifier(@Nullable String serviceIdentifier) {
		this.serviceIdentifier = serviceIdentifier;
	}

	/**
	 * Get the service properties.
	 * 
	 * @return the serviceProperties
	 */
	public final @Nullable Map<String, Object> getServiceProperties() {
		return serviceProperties;
	}

	/**
	 * Set the service properties.
	 * 
	 * @param serviceProperties the serviceProperties to set
	 */
	public final void setServiceProperties(@Nullable Map<String, Object> serviceProperties) {
		this.serviceProperties = serviceProperties;
	}

}
