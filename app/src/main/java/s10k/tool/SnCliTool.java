package s10k.tool;

import java.time.LocalDateTime;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ReflectiveScan;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.common.util.StringUtils;
import s10k.tool.datum.cmd.DatumCmd;
import s10k.tool.flux.cmd.FluxCmd;
import s10k.tool.instructions.cmd.InstructionsCmd;
import s10k.tool.nodes.cmd.NodesCmd;

/**
 * SolarNetwork command-line tool.
 */
@SpringBootApplication
@ReflectiveScan("s10k.tool")
public class SnCliTool implements CommandLineRunner, ExitCodeGenerator {

	private final IFactory factory;

	private int exitCode;

	/**
	 * Constructor.
	 * 
	 * @param factory the command factory
	 */
	public SnCliTool(IFactory factory) {
		super();
		this.factory = factory;
	}

	@Override
	public void run(String... args) throws Exception {
		final var app = new ToolCmd();
		System.setProperty("picocli.converters.excludes", "java.time.LocalDateTime");
		// @formatter:off
		exitCode = new CommandLine(app, factory)
				.setExecutionStrategy(app::globalInit)
				.registerConverter(LocalDateTime.class, StringUtils::parseLocalDateTime)
				.addSubcommand(new DatumCmd())
				.addSubcommand(new FluxCmd())
				.addSubcommand(new InstructionsCmd())
				.addSubcommand(new NodesCmd())
				.execute(args);
		// @formatter:on
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}

	/**
	 * Main entry point.
	 * 
	 * @param args the arguments
	 */
	public static final void main(String[] args) {
		System.exit(SpringApplication.exit(new SpringApplicationBuilder().sources(SnCliTool.class)
				.web(WebApplicationType.NONE).logStartupInfo(false).bannerMode(Mode.OFF).build().run(args)));
	}

}