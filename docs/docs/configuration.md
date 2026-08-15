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

## Configuration settings

The following settings are supported in the configuration file:

| Key | Description |
|:----|:------------|
| [`datum-stream-aliases`](#datum-stream-aliases) | Globally include/exclude datum stream aliases where supported. |
| [`display-mode`](#default-display-mode) | Set the default display mode. |
| [`pretty-style`](#pretty-style) | Set the style to use for `PRETTY` display mode. |
| [`service-urls`](#service-urls) | Configure custom SolarNetwork API endpoints. |
| [`zone`](#display-time-zone) | Set the time zone to use for display. |

### Datum stream aliases

The `datum-stream-aliases` setting allows configuring a default "include datum stream aliases" mode
when querying for datum. This is `true` by default, so you can turn off aliases inclusion by
configuring this setting to `false`.

### Default display mode

The `display-mode` setting allows configuring a default display mode for command results.
Many commands offer a `--display-mode` option that can override this global configuration.

### Pretty style

The `pretty-style` setting allows configuring the style to use in the `PRETTY` display mode for
command results. The supported values are `basic` (the default) or `fancy` for a more modern look.

=== "Basic style"

	```
	+-----------------+-------+----------------------------------+---------------------------+---------+
	| Datum Stream ID | State | Execute At                       | Start At                  | Message |
	+-----------------+-------+----------------------------------+---------------------------+---------+
	|               1 | q     | 2026-08-15 15:00:00+12:00        | 2026-08-15 14:45:00+12:00 |         |
	+-----------------+-------+----------------------------------+---------------------------+---------+
	|               2 | q     | 2026-08-15 14:42:29.123372+12:00 | 2026-08-15 14:15:00+12:00 |         |
	+-----------------+-------+----------------------------------+---------------------------+---------+
	|               3 | q     | 2026-08-15 15:00:00+12:00        | 2026-08-15 14:45:00+12:00 |         |
	+-----------------+-------+----------------------------------+---------------------------+---------+
	```

=== "Fancy style"

	```
	╔═════════════════╤═══════╤══════════════════════════════════╤═══════════════════════════╤═════════╗
	║ Datum Stream ID │ State │ Execute At                       │ Start At                  │ Message ║
	╠═════════════════╪═══════╪══════════════════════════════════╪═══════════════════════════╪═════════╣
	║               1 │ q     │ 2026-08-15 15:00:00+12:00        │ 2026-08-15 14:45:00+12:00 │         ║
	╟─────────────────┼───────┼──────────────────────────────────┼───────────────────────────┼─────────╢
	║               2 │ q     │ 2026-08-15 14:42:29.123372+12:00 │ 2026-08-15 14:15:00+12:00 │         ║
	╟─────────────────┼───────┼──────────────────────────────────┼───────────────────────────┼─────────╢
	║               3 │ q     │ 2026-08-15 15:00:00+12:00        │ 2026-08-15 14:45:00+12:00 │         ║
	╚═════════════════╧═══════╧══════════════════════════════════╧═══════════════════════════╧═════════╝
	```

### Service URLs

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

### Display time zone

The `zone` setting allows configuring a default display time zone, to use when rendering
date values. Note that in `JSON` display mode this setting is ignored.

