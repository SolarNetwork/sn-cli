# Install

The easiest way to install `s10k` is through [Homebrew](#homebrew-macoslinux). Otherwise
you can [manually](#manual-macoslinuxwindows) download and install the application.

## Homebrew (macOS/Linux)

You can install the `s10k` native binary using [Homebrew](https://brew.sh/) by adding the
`SolarNetwork/tap` tap's `sn-cli` package:

```sh
# add the SolarNetwork tap
brew tap solarnetwork/tap

# trust the SolarNetwork tap
brew trust solarnetwork/tap

# install the sn-cli package
brew install sn-cli
```

After that, the `s10k` tool should be available:

```sh
s10k version
0.15.0
```

## Manual (macOS/Linux/Windows)

Download the [latest release](https://github.com/SolarNetwork/sn-cli/releases) from Github. Download
the appropriate binary archive for your system, if available. Otherwise download the JAR file.

=== "Linux (arm64)"

	Download the tar archive named like `sn-cli-X.Y.Z-linux-arm64.tar.gz`. Use `tar` to expand the archive,
	and then move the extracted `s10k` application somewhere on your `PATH` like `/usr/local/bin`.

=== "Linux (x64)"

	Download the tar archive named like `sn-cli-X.Y.Z-linux-amd64.tar.gz`. Use `tar` to expand the
	archive, and then move the extracted `s10k` application somewhere on your `PATH` like
	`/usr/local/bin`.

=== "macOS (Apple Silicon)"

	Download the tar archive named like `sn-cli-X.Y.Z-darwin-arm64.tar.gz`. Double-click to expand
	the archive, or use `tar`, and then move the extracted `s10k` application somewhere on your
	`PATH` like `/usr/local/bin`.

=== "Windows (x64)"

	Download the tar archive named like `sn-cli-X.Y.Z-windows-amd64.tar.gz `. Expand the archive,
	and then move the extracted `s10k` application somewhere on your `PATH`.

If a binary is not available for your system, you can download the Java JAR, which requires you
to have Java 21+ installed.

=== "Java JAR"

	Download the JAR file named like `s10k-X.Y.Z.jar`. You can execute this using
	`java -jar s10k-X.Y.Z.jar` (followed by the command and options).

