---
title: save
---
# Nodes Meta Save

Save metadata associated with a node.

# Usage

```
s10k nodes meta save [-r] -node=<nodeId> [<metadata>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to save metadata to |
| `-r` | `--replace` | replace **all** existing metadata, rather than add/update |

</div>

## Output

A success message, followed by a pretty-printed copy of the saved metadata.

## Metadata structure

SolarNetwork defines [metadata][metadata] as two tree-like structures along with a set of tags.
First there is the `m` structure that defines a simple key-value pair of simple values (like
strings, booleans, and numbers). Then there is the `pm` structure that defines an arbitrarily-nested
tree structure. Then there is the `t` set of tags. An example looks like this:

```json
{
  "m" : {
    "limit" : 10000,
	"group" : "alpha"
  },
  "pm" : {
    "os" : {
      "arch" : "aarch64",
      "name" : "Linux",
      "version" : "6.1.21-v8+"
    }
  },
  "t" : ["west", "red"]
}
```

## Examples

The metadata to save can be provided directly as a command argument, for example:

```sh title="Metadata as command argument"
s10k --profile demo nodes meta save --node-id 101 '{"m":{"limit":123}}'
```

A file with the metadata can be referenced using `@@` followed by the file path:

```sh title="Metadata as a file"
s10k --profile demo nodes meta save --node-id 101 @@/path/to/metadata.json
```

The metadata content can be read from standard input, like this:

```sh title="Metadata read from standard input"
s10k --profile=demo nodes meta save -node 1011 </path/to/metadata.json
```

Similarly, the metadata content can be piped to the command, like this:

=== "Example"

	```sh title="Metadata piped from standard input"
	jq -n --argjson  limit 123 '{"m":{"limit":$limit}}' \
	  |s10k --profile demo nodes meta save --node-id 101
	```

=== "Output"

	```
	Node metadata added:
	{
		"m" : {
			"limit" : 123
		}
	}
	```

[metadata]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#metadata
