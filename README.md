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

To build a release, using `git flow`:

```sh
export S10K_RELEASE_VERSION=1.0.0

# start release
git flow release start $RELEASE_VERSION

# macOS
for f in gradle.properties app/src/main/resources/s10k/tool/version.properties; do \
  sed -i '' -e 's/^version = .*/version = '"$S10K_RELEASE_VERSION"'/' $f; \
  done
  
# or, Linux
for f in gradle.properties app/src/main/resources/s10k/tool/version.properties; do \
  sed -i -e 's/^version = .*/version = '"$S10K_RELEASE_VERSION"'/' $f; \
  done

# commit version updates
git add .
git commit -S -m 'Bump version for next release.'

# finish release
git flow release finish -s

# push changes
git push --all && git push --tags

# jump back to develop branch
git switch develop
``` 

[graalvm]: https://www.graalvm.org/
[logging-conf]: https://docs.spring.io/spring-boot/reference/features/logging.html
