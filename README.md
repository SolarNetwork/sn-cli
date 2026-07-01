# SolarNetwork Command Line Tool

This project contains a command line (CLI) tool for interacting with the SolarNetwork API. The tool
allows you to do things like:

 * discover available nodes
 * discover available datum streams
 * list persisted datum
 * update SolarNode control values
 * toggle SolarNode operating modes
 * manage SolarNode metadata
 * stream datum from SolarFlux
 * and more! See the [documentation](https://solarnetwork.github.io/sn-cli/) for more information.

# Get

Download the [latest release](https://github.com/SolarNetwork/sn-cli/releases), either as an
executable binary for your operating system or an executable Java JAR you can run anywhere you have
Java 25+ installed.

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
0.13.0
```

# Logging

Logging can be enabled by creating an `application.yml` file in your working directory. You can then
configure standard [Spring Boot Logging][logging-conf] settings. For example if you would like HTTP
exchange traces, add the `--http-trace` option and then configure logging something like this:

```yaml
logging:
  file.name: "/var/tmp/s10k.log"
  level:
    net.solarnetwork.http: "TRACE"
  threshold:
    console: "INFO"
    file: "TRACE"
```

# Building from source

To build the executable JAR application from source, run:

```sh
# Unix
./gradlew bootJar

# Windows
.\gradlew.bat bootJar
```

The application will be built to `app/build/libs/s10k-VERSION.jar`.

## Building the native binary

To build the native binary, you must have the [GraalVM][graalvm] version 25+ or later installed.
Then add `GRAALVM_HOME` to your environment. For example in `sh`:

```sh
# macOS install
export GRAALVM_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.jdk/Contents/Home
```

Then you can run:

```sh
# Unix
./gradlew nativeCompile

# Windows
.\gradlew.bat nativeCompile
```

The native binary will be built to `app/build/native/nativeCompile/s10k`.

### Building a native distribution archive

Execute the `distTar` or `distZip` task to create an archive of the native application:

```sh
# Unix
./gradlew distTar

# Windows
.\gradlew.bat distZip
```

The archive will be created in the `app/build/distributions` directory and named like
`sn-cli-${VERSION}-${OS}-${ARCH}.${EXT}`, for example:

| OS | Architecture | Archive | Output suffix |
|:---|:-------------|:--------|:--------------|
| Linux | x86 64-bit | Tar | `-linux-amd64.tar.gz` |
| Linux | ARM 64-bit | Tar | `-linux-arm64.tar.gz` |
| macOS | ARM 64-bit | Tar | `-macos-arm64.tar.gz` |
| Windows | x86 64-bit | Zip | `-windows-amd64.zip` |

Formally, `${OS}` is one of:

| OS | Archive name |
|:---|:-------------|
| Linux | `linux` |
| macOS | `macos` |
| Windows | `windows` |

and `${ARCH}` is one of:

| Architecture | Archive name |
|:-------------|:-------------|
| ARM 64-bit | `arm64` |
| x86 64-bit | `amd64` |

and `${EXT}` is one of:

| Archive | Archive name |
|:-------------|:-------------|
| Tar | `tar.gz` |
| Zip | `zip` |

# Building a release

To build a release, use the `config/tools/release.sh` script, passing the desired release version
as an argument. For example:

```sh
# can dry-run with -n option
./config/tools/release.sh -n 1.0.0

# or run for real
./config/tools/release.sh 1.0.0
```

You must have **Gitflow** installed for the release script to work, and you must have a GPG signing
key configured. The script will:

 1. Create a new git flow release with the given version number
 2. Update the version property files with the given version number
 3. Commit the property file changes to git
 4. Finish the git flow release (tagging with the given version number)
 5. Push all changes up to the origin repo
 6. Switch to the develop branch
 7. Update the version property files with the "next" development version
 8. Commit the property file changes to git
 9. Push all changes, and tags, up to the origin repo

[graalvm]: https://www.graalvm.org/
[logging-conf]: https://docs.spring.io/spring-boot/reference/features/logging.html
