package s10k.tool.instructions.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.instructions.controls.cmd.ControlsCmd;
import s10k.tool.instructions.settings.cmd.SettingsCmd;

/**
 * Instruction commands.
 */
// @formatter:off
@Command(name = "instructions", aliases = "instr", subcommands = {
		ControlsCmd.class,
		ListComponents.class,
		ListControlsCmd.class,
		ListPackagesCmd.class,
		ListInstructionsCmd.class,
		ListServicesCmd.class,
		SettingsCmd.class,
		ToggleOperationalModeCmd.class,
		UpdateInstructionsState.class,
})
// @formatter:on
public class InstructionsCmd extends BaseSubCmd<ToolCmd> {

	/**
	 * The instruction topic for applying system configuration.
	 * 
	 * <p>
	 * A {@link #PARAM_SERVICE} parameter must be provided that specifies the system
	 * service to apply the configuration to. Each service may define additional
	 * parameters that can be configured.
	 * </p>
	 */
	public static final String TOPIC_SYSTEM_CONFIGURE = "SystemConfigure";

	/**
	 * The instruction topic for obtaining system configuration.
	 * 
	 * <p>
	 * A {@link #PARAM_SERVICE} parameter must be provided that specifies the system
	 * service to get the configuration for. Each service may define additional
	 * parameters that can be configured.
	 * </p>
	 * 
	 * @see #TOPIC_SYSTEM_CONFIGURE for applying system configuration updates
	 */
	public static final String TOPIC_SYSTEM_CONFIGURATION = "SystemConfiguration";

	/**
	 * An instruction parameter for a service name.
	 * 
	 * <p>
	 * The nature of this parameter depends on the topic it is associated with.
	 * Generally it is meant to refer to the name of some service to be operated on.
	 * </p>
	 */
	public static final String PARAM_SERVICE = "service";

	/**
	 * An instruction parameter for a service argument.
	 * 
	 * <p>
	 * The nature of this parameter depends on the topic it is associated with.
	 * Generally it is meant to refer to an argument to pass to a service to be
	 * operated on.
	 * </p>
	 */
	public static final String PARAM_SERVICE_ARGUMENT = "arg";

	/**
	 * An instruction parameter for a service result.
	 * 
	 * <p>
	 * The nature of this parameter depends on the topic it is associated with.
	 * Generally it is meant to refer to the result of some service execution.
	 * </p>
	 */
	public static final String PARAM_SERVICE_RESULT = "result";

	/**
	 * A standard result parameter key for a message (typically an error message).
	 */
	public static final String PARAM_MESSAGE_RESULT = "message";

	/** A standard result parameter key for an error code. */
	public static final String PARAM_ERROR_CODE_RESULT = "code";

	/** The service name for settings operations. */
	public static final String SYSTEM_CONFIGURATION_SETTINGS_SERVICE = "net.solarnetwork.node.settings";

}
