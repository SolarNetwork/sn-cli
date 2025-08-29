# SolarNetwork CLI

This handbook provides guides and reference documentation for the
[SolarNetwork](https://solarnetwork.net/) command line interface (CLI) tool, `s10k`. This is a
_command oriented_ tool where each [command](commands/index.md) supports its own set of options,
along with some [global options](global-options.md).

Here is an example of running `s10k` to display the metadata associated with a SolarNode, using the
`nodes meta list` command:

```sh
s10k --profile demo nodes meta list --node-id 101

Property Value
------------------------------------
nodeId   101
created  2025-08-28T23:22:00.237150Z
updated  2025-08-28T23:22:00.237150Z
{
  "pm" : {
    "os" : {
      "arch" : "aarch64",
      "name" : "Linux",
      "version" : "6.1.21-v8+"
    }
  }
}
```
