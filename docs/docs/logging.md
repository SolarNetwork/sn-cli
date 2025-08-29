# Logging

Debug logging can be enabled by creating an `application.yml` file in your working directory. You
can then configure standard [Spring Boot Logging][logging-conf] settings. For example if you would
like HTTP exchange traces, use the `--http-trace` [global option](global-options.md) and then
configure `application.yml` something like this:

```yaml
logging:
  file.name: "/var/tmp/s10k.log"
  level:
    net.solarnetwork.http: "TRACE"
  threshold:
    console: "OFF"
    file: "TRACE"
```

[logging-conf]: https://docs.spring.io/spring-boot/reference/features/logging.html
