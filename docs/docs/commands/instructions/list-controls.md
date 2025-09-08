---
title: list-controls
---
# Instructions List-Controls

List the available control IDs on a node.

## Usage

```
s10k instructions list-controls -node=<nodeId> [-filter=<filter>]
                                [-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to list the control IDs for |
| `-filter=` | `--filter=` | a [wildcard pattern][wildcard] to restrict the results to |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of all matching instruction records.

## Examples

You can list all available control IDs on a node like this:

=== "Show available controls"

	```sh
	s10k instructions list-controls --node-id 101
	```

=== "Pretty output"

	```
	+------------+
	| Control ID |
	+------------+
	| pcm/limit  |
	+------------+
	| switch/1   |
	+------------+
	```

=== "CSV output"

	```csv
	Control ID
	pcm/limit
	switch/1
	```

=== "JSON output"

	```json
	[
		"test/pcm/limit",
		"test/switch/1"
	]
	```


[wildcard]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#wildcard-patterns
