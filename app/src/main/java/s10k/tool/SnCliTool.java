package s10k.tool;

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
import s10k.tool.instructions.cmd.InstructionsCmd;

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
		// @formatter:off
		exitCode = new CommandLine(app, factory)
				.setExecutionStrategy(app::globalInit)
				.addSubcommand(new InstructionsCmd())
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