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

You could use the output of this command to find the current (reported) values of
all node controls with the [datum list](../datum/list.md) command like this:

=== "Show current value of available controls"

	```sh
	s10k datum list --node-id 101 --most-recent --source-id $( \
		s10k instructions list-controls --node-id 101 \
		  --display-mode JSON |jq -r 'join(",")'
	)
	```

=== "Pretty output"

	```
	+--------------------------+-----------+-----------+-----+-----+
	| Timestamp                | Object ID | Source ID | v   | val |
	+--------------------------+-----------+-----------+-----+-----+
	| 2025-09-08T04:19:10.194Z | 101       | pcm/limit | 100 | 100 |
	+--------------------------+-----------+-----------+-----+-----+
	| 2025-09-08T04:19:10.139Z | 101       | switch/1  |   0 |     |
	+--------------------------+-----------+-----------+-----+-----+
	```

=== "CSV output"

	```csv
	ts,streamId,objectId,sourceId,v,val,tags
	2025-09-08T04:19:10.194Z,c8560910-9920-4d57-a1a9-d4e4722cbcdd,101,pcm/limit,100,100,
	2025-09-08T04:19:10.139Z,f68e81cd-2ac3-4760-b449-16ebce64c15a,101,switch/1,,0,
	```

=== "JSON output"

	```json
	{
		"success": true,
		"meta": [
			{
				"streamId": "c8560910-9920-4d57-a1a9-d4e4722cbcdd",
				"zone": "Pacific/Auckland",
				"kind": "n",
				"objectId": 101,
				"sourceId": "pcm/limit",
				"i": [
					"v"
				],
				"s": [
					"val"
				]
			},
			{
				"streamId": "f68e81cd-2ac3-4760-b449-16ebce64c15a",
				"zone": "Pacific/Auckland",
				"kind": "n",
				"objectId": 101,
				"sourceId": "switch/1",
				"s": [
					"val"
				]
			}
		],
		"data": [
			[
				0,
				1757305150194,
				100,
				"100"
			],
			[
				1,
				1757305150139,
				"0"
			]
		]
	}
	```


[wildcard]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#wildcard-patterns
