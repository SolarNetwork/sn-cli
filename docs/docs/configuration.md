# Configuration

Some aspects of `s10k` can be modified through a configuration file.

## Configuration location

The configuration is stored in a `$HOME/.s10k/config` file, where `$HOME` is your system user's
"home" directory. For example on macOS for a user `alice` this might look like
`/Users/alice/.s10k/config` or on Linux `/home/alice/.s10k/config`.

## Configuration structure

The configuration file is a [TOML](https://toml.io) file, where you can include **default**
configuration followed by named [profile](./profiles.md) sections under a `[profile]` section.
Within each section are `key = "value"` configuration lines. For example, here is a `config` file
with a default setting followed by two profiles named **home** and **work**:

```toml
some-setting = "some-value"

[profile.home]
some-setting = "some-value-for-home"

[profile.work]
some-setting = "some-value-for-work"
```

## Service URLs

The `service-urls` setting allows configuring alternate base URLs for SolarNetwork services.
When HTTP requests are made to SolarNetwork services, these alternate URLs will be used
instead of the default SolarNetwork ones.

The value of this setting is a TOML map of _service keys_ to associated base URL strings.

The supported _service key_ values are:

| Service Key | Description |
|:------------|:------------|
| `solarquery` | The base URL to the SolarQuery API. |
| `solaruser`  | The base URL to the SolarUser API. |

For example, here is a configuration file that alters the SolarNetwork API URLs to
`localhost`, useful for developers working on `s10k` itself:

```toml
[profile.dev.service-urls]
solarquery = "http://localhost:9082"
solaruser  = "http://localhost:9081"
```
