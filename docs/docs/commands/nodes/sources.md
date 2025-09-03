---
title: sources
---
# Node Sources

List node sources matching a search filter.

!!! info

	For more details on the SolarNetwork APIs used by this command, see the documentation for the
	[/nodes/sources](https://github.com/SolarNetwork/solarnetwork/wiki/SolarQuery-API#reportable-node-sources)
	endpoint.

# Usage

```
s10k nodes sources [-node=nodeId[,nodeId...]]...
					[-source=sourceId[,sourceId...]]...
					[-min=<minDate>] [-max=<maxDate>]
					[-local] [-tz=<zone>]
					[-filter=<metadataFilter>]
					[-prop=propName[,propName...]]...
					[-i=propName[,propName...]]...
                    [-a=propName[,propName...]]...
					[-s=propName[, propName...]]...
					[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID(s) to show metadata for |
| `-node=` | `--node-id=` | the node ID(s) to show stream metadata for (exclusive to `-loc`) |
| `-source=` | `--source=` | the source ID(s) to show stream metadata for |
| `-min=` | `--min-date=` | a minimum date to limit results to, like `2020-10-30` or `2020-10-30T12:45` |
| `-max=` | `--max-date=` | a maximum date (exclusive) to limit results to, in same form as `-min` |
| `-local` | `--local-dates` | treat the min/max dates as "node local" dates, instead of UTC (or the local time zone when `-tz` used) |
| `-tz=` | `--time-zone=` | a time zone ID to treat the min/max dates as, instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-filter=` | `--filter=` | an optional [metadata filter][metadata-filter] to limit results to |
| `-prop=` | `--property=` | restrict results to metadata that has this property (instantaneous, accumulating, **or** status); multiple properties combine with logical "or" |
| `-i=` | `--instantaneous=` | restrict results to metadata that has this **instantaneous** property; multiple properties combine with logical "and" |
| `-a=` | `--accumulating=` | restrict results to metadata that has this **accumulating** property; multiple properties combine with logical "and" |
| `-s=` | `--status=` | restrict results to metadata that has this **status** property; multiple properties combine with logical "and" |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY`; **note** that `PRETTY` is not suitable for large result sets |

</div>

## Output

A listing of all matching node sources.

## Examples

List all available sources:

=== "List all sources"

	```sh
	s10k nodes sources
	```

=== "Pretty Output"

	```
	+---------+---------------+
	| Node ID | Source ID     |
	+---------+---------------+
	|      64 | /meter/1      |
	+---------+---------------+
	|      64 | /meter/2      |
	+---------+---------------+
	|      64 | /test/solcast |
	+---------+---------------+
	|      70 | meter/1       |
	+---------+---------------+
	|     101 | con/1         |
	+---------+---------------+
	|     101 | con/pcm       |
	+---------+---------------+
	|     101 | dnp3/client/a |
	+---------+---------------+
	|     101 | gen/1         |
	+---------+---------------+
	|     101 | pcm/limit     |
	+---------+---------------+
	|     101 | power/limit   |
	+---------+---------------+
	|     101 | switch/1      |
	+---------+---------------+
	```

=== "CSV Output"

	```csv
	Node ID,Source ID
	64,/meter/1
	64,/meter/2
	64,/test/solcast
	70,meter/1
	101,con/1
	101,con/pcm
	101,dnp3/client/a
	101,gen/1
	101,pcm/limit
	101,power/limit
	101,switch/1
	```

=== "JSON Output"

	```json
	[
		{
			"kind" : "n",
			"objectId" : 64,
			"sourceId" : "/meter/1"
		},
		{
			"kind" : "n",
			"objectId" : 64,
			"sourceId" : "/meter/2"
		},
		{
			"kind" : "n",
			"objectId" : 64,
			"sourceId" : "/test/solcast"
		},
		{
			"kind" : "n",
			"objectId" : 70,
			"sourceId" : "meter/1"
		},
		{
			"kind" : "n",
			"objectId" : 101,
			"sourceId" : "con/1"
		},
		{
			"kind" : "n",
			"objectId" : 101,
			"sourceId" : "con/pcm"
		},
		{
			"kind" : "n",
			"objectId" : 101,
			"sourceId" : "dnp3/client/a"
		},
		{
			"kind" : "n",
			"objectId" : 101,
			"sourceId" : "gen/1"
		},
		{
			"kind" : "n",
			"objectId" : 101,
			"sourceId" : "pcm/limit"
		},
		{
			"kind" : "n",
			"objectId" : 101,
			"sourceId" : "power/limit"
		},
		{
			"kind" : "n",
			"objectId" : 101,
			"sourceId" : "switch/1"
		}
	]
	```

Show sources that have both `watts` instantaneous and `wattHours` accumulating properties
and have posted datum since `2025-08-01`:

```sh
s10k datum sources \
  --instantaneous watts \
  --accumulating wattHours \
  --min-date 2025-08-01
```


[metadata-filter]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#metadata-filter
