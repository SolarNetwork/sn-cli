---
title: list
---
# Datum Stream List

Show [datum stream metadata][datum-stream-meta] matching a search filter.

# Usage

```
s10k datum stream list [-mode=<displayMode>]
                       [-source=sourceId[,sourceId...]]...
					   [-stream=streamId[,streamId...]]...
					   [
						 -node=nodeId[,nodeId...][-node=nodeId[,nodeId...]]... |
						 -loc=locId[,locId...] [-loc=locId[,locId...]]...
						]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the stream ID(s) to show |
| `-node=` | `--node-id=` | the node ID(s) to show stream metadata for (exclusive to `-loc`) |
| `-loc=` | `--location-id=` | the location ID(s) to show stream metadata for (exclusive to `-node`) |
| `-source=` | `--source=` | the source ID(s) to show stream metadata for |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of all matching stream metadata.

## Examples

View all datum stream metadata for a node:

=== "Show datum stream metadata for node ID"

	```sh
	s10k --profile demo datum stream list --node-id 101
	```

=== "Pretty Output"

	```
	+--------------------------------------+------+-----+---------------+------------------+---------------------------------+--------------+--------+
	| Stream ID                            | Kind | ID  | Source ID     | Time Zone        | Instantaneous                   | Accumulating | Status |
	+--------------------------------------+------+-----+---------------+------------------+---------------------------------+--------------+--------+
	| f68e81cd-2ac3-4760-b449-16ebce64c15a | Node | 101 | switch/1      | Pacific/Auckland |                                 |              | val    |
	+--------------------------------------+------+-----+---------------+------------------+---------------------------------+--------------+--------+
	| 6718cc51-e5fb-43a9-a33f-344bc34916f2 | Node | 101 | power/limit   | Pacific/Auckland | v                               |              | val    |
	+--------------------------------------+------+-----+---------------+------------------+---------------------------------+--------------+--------+
	| cc114908-cc0f-4680-a92e-718690742ba9 | Node | 101 | gen/1         | Pacific/Auckland | current,frequency,voltage,watts | wattHours    |        |
	+--------------------------------------+------+-----+---------------+------------------+---------------------------------+--------------+--------+
	```

=== "CSV Output"

	```csv
	Stream ID,Kind,ID,Source ID,Time Zone,Instantaneous,Accumulating,Status
	f68e81cd-2ac3-4760-b449-16ebce64c15a,Node,101,switch/1,Pacific/Auckland,,,val
	6718cc51-e5fb-43a9-a33f-344bc34916f2,Node,101,power/limit,Pacific/Auckland,v,,val
	cc114908-cc0f-4680-a92e-718690742ba9,Node,101,gen/1,Pacific/Auckland,"current,frequency,voltage,watts",wattHours,
	```

=== "JSON Output"

	```json
	[ {
	"streamId" : "f68e81cd-2ac3-4760-b449-16ebce64c15a",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "switch/1",
	"s" : [ "val" ]
	}, {
	"streamId" : "6718cc51-e5fb-43a9-a33f-344bc34916f2",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "power/limit",
	"i" : [ "v" ],
	"s" : [ "val" ]
	}, {
	"streamId" : "cc114908-cc0f-4680-a92e-718690742ba9",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "gen/1",
	"i" : [ "watts", "current", "voltage", "frequency" ],
	"a" : [ "wattHours" ]
	} ]
	```

[datum-stream-meta]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#datum-stream-metadata
