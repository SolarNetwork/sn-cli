# SolarNetwork Command Line Tool

This project contains a command line (CLI) tool for interacting with the SolarNetwork API. The tool
allows you to do things like:

 * discover available nodes
 * discover available datum streams
 * list persisted datum
 * update SolarNode control values
 * toggle SolarNode operating modes
 * stream datum from SolarFlux
 * and more! See the [documentation](https://solarnetwork.github.io/sn-cli/) for more information.

# Logging

Logging can be enabled by creating an `application.yml` file in your working directory. You can then
configure standard [Spring Boot Logging][logging-conf] settings. For example
if you would like HTTP exchange traces, add the `--http-trace` option and then configure logging
something like this:

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
./gradlew build -x test

# Windows
.\gradlew.bat build -x test
```

The application will be built to `build/libs/s10k-VERSION.jar`.

## Building the native binary

To build the native binary, you must have the [GraalVM][graalvm] version 21+ or later installed.
Then add `GRAALVM_HOME` to your environment. For example in `sh`:

```sh
# macOS install
export GRAALVM_HOME=/Library/Java/JavaVirtualMachines/graalvm-21.jdk/Contents/Home
```

Then you can run:

```sh
# Unix
./gradlew nativeCompile

# Windows
.\gradlew.bat nativeCompile
```

The native binary will be built to `app/build/native/nativeCompile/s10k`.

[graalvm]: https://www.graalvm.org/
[logging-conf]: https://docs.spring.io/spring-boot/reference/features/logging.html
