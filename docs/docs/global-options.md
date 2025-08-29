# Global options

Some "global" options are available irrespective of the [command](commands/index.md) you are running.
They must be provided _before_ the command and any subsequent command-specific options. The following global options are supported:

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-h` | `--help` | Display help information |
| `-P` | `--profile` | Select a [profile](profiles.md) to use |
|  | `--http-trace` | Enable HTTP exchange trace logging. Must also configure the `net.solarnetwork.http.REQ` and/or `net.solarnetwork.http.RES` [logger levels](logging.md) to `TRACE`. |

</div>
