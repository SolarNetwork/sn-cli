---
title: ids
---
# Datum Stream Ids

Show [datum stream metadata][datum-stream-meta] IDs matching a search filter.

## Usage

```
s10k datum stream ids [-mode=<displayMode>]
                      [-source=sourceId[,sourceId...]]...
					  [-stream=streamId[,streamId...]]...
					  [
						-node=nodeId[,nodeId...][-node=nodeId[,nodeId...]]... |
						-loc=locId[,locId...] [-loc=locId[,locId...]]...
					   ]
					  [-prop=propName[,propName...]]...
					  [-a=propName[,propName...]]...
					  [-i=propName[,propName...]]...
					  [-s=propName[,propName...]]...
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
| `-prop=` | `--property=` | restrict results to metadata that has this property (instantaneous, accumulating, **or** status); multiple properties combine with logical "or" |
| `-i=` | `--instantaneous=` | restrict results to metadata that has this **instantaneous** property; multiple properties combine with logical "and" |
| `-a=` | `--accumulating=` | restrict results to metadata that has this **accumulating** property; multiple properties combine with logical "and" |
| `-s=` | `--status=` | restrict results to metadata that has this **status** property; multiple properties combine with logical "and" |

</div>

## Output

A listing of all matching stream metadata.

## Examples

View all datum stream metadata IDs for a node:

=== "Show datum stream metadata IDs for node"

	```sh
	s10k datum stream ids --node-id 101
	```

=== "Pretty Output"

	```
	+--------------------------------------+------+-----+---------------+
	| Stream ID                            | Kind | ID  | Source ID     |
	+--------------------------------------+------+-----+---------------+
	| f68e81cd-2ac3-4760-b449-16ebce64c15a | Node | 101 | switch/1      |
	+--------------------------------------+------+-----+---------------+
	| 6718cc51-e5fb-43a9-a33f-344bc34916f2 | Node | 101 | power/limit   |
	+--------------------------------------+------+-----+---------------+
	| cc114908-cc0f-4680-a92e-718690742ba9 | Node | 101 | gen/1         |
	+--------------------------------------+------+-----+---------------+
	```

=== "CSV Output"

	```csv
	Stream ID,Kind,ID,Source ID
	f68e81cd-2ac3-4760-b449-16ebce64c15a,Node,101,switch/1
	6718cc51-e5fb-43a9-a33f-344bc34916f2,Node,101,power/limit
	cc114908-cc0f-4680-a92e-718690742ba9,Node,101,gen/1
	```

=== "JSON Output"

	```json
	[ {
	"streamId" : "f68e81cd-2ac3-4760-b449-16ebce64c15a",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "switch/1"
	}, {
	"streamId" : "6718cc51-e5fb-43a9-a33f-344bc34916f2",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "power/limit"
	}, {
	"streamId" : "cc114908-cc0f-4680-a92e-718690742ba9",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "gen/1"
	} ]
	```

View all datum stream metadata IDs for a node that has some property named `watts`:

=== "Show datum stream metadata IDs for node with _watts_ property"

	```sh
	s10k datum stream ids --node-id 101 --property watts
	```

=== "Pretty Output"

	```
	+--------------------------------------+------+-----+-----------+
	| Stream ID                            | Kind | ID  | Source ID |
	+--------------------------------------+------+-----+-----------+
	| cc114908-cc0f-4680-a92e-718690742ba9 | Node | 101 | gen/1     |
	+--------------------------------------+------+-----+-----------+
	| 0c0fc45d-1e96-40cb-8aa0-07619b3158fd | Node | 101 | con/pcm   |
	+--------------------------------------+------+-----+-----------+
	| 03c6bd01-9241-4771-ad3c-a1d5eb06b68a | Node | 101 | con/1     |
	+--------------------------------------+------+-----+-----------+
	```

=== "CSV Output"

	```csv
	Stream ID,Kind,ID,Source ID
	cc114908-cc0f-4680-a92e-718690742ba9,Node,101,gen/1
	0c0fc45d-1e96-40cb-8aa0-07619b3158fd,Node,101,con/pcm
	03c6bd01-9241-4771-ad3c-a1d5eb06b68a,Node,101,con/1
	```

=== "JSON Output"

	```json
	[ {
	"streamId" : "cc114908-cc0f-4680-a92e-718690742ba9",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "gen/1"
	}, {
	"streamId" : "0c0fc45d-1e96-40cb-8aa0-07619b3158fd",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "con/pcm"
	}, {
	"streamId" : "03c6bd01-9241-4771-ad3c-a1d5eb06b68a",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "con/1"
	} ]
	```

Find all source IDs for a node where the datum stream has an instantaneous property named `voltage`
and an accumulating property named `wattHours`:

=== "Find source IDs with _voltage_, _wattHours_ properties"

	```sh
	s10k datum stream ids --instantaneous voltage \
	  --accumulating wattHours --mode JSON \
	  |jq -r 'map(.sourceId) | join(",")'
	```

=== "Output"

	```
	con/1,con/pcm,gen/1
	```

View all datum stream metadata IDs that have an instantaneous property named `pcmLimit`
and an accumulating property named `wattHours`:

=== "Show datum stream metadata IDs with _pcmLimit_, _wattHours_ properties"

	```sh
	s10k datum stream ids --instantaneous pcmLimit \
	  --accumulating wattHours
	```

=== "Pretty Output"

	```
	+--------------------------------------+------+-----+-----------+
	| Stream ID                            | Kind | ID  | Source ID |
	+--------------------------------------+------+-----+-----------+
	| 2e16deac-bd95-4d4b-9668-4441f48aea17 | Node |  64 | /meter/1  |
	+--------------------------------------+------+-----+-----------+
	| a73f4797-d9aa-4496-b96c-04ad934b1550 | Node |  64 | /meter/2  |
	+--------------------------------------+------+-----+-----------+
	| 2d19d690-2472-480d-afc0-240f830d4cd5 | Node |  70 | meter/1   |
	+--------------------------------------+------+-----+-----------+
	| 03c6bd01-9241-4771-ad3c-a1d5eb06b68a | Node | 101 | con/1     |
	+--------------------------------------+------+-----+-----------+
	| 0c0fc45d-1e96-40cb-8aa0-07619b3158fd | Node | 101 | con/pcm   |
	+--------------------------------------+------+-----+-----------+
	| cc114908-cc0f-4680-a92e-718690742ba9 | Node | 101 | gen/1     |
	+--------------------------------------+------+-----+-----------+
	```

=== "CSV Output"

	```csv
	Stream ID,Kind,ID,Source ID
	2e16deac-bd95-4d4b-9668-4441f48aea17,Node,64,/meter/1
	a73f4797-d9aa-4496-b96c-04ad934b1550,Node,64,/meter/2
	2d19d690-2472-480d-afc0-240f830d4cd5,Node,70,meter/1
	03c6bd01-9241-4771-ad3c-a1d5eb06b68a,Node,101,con/1
	0c0fc45d-1e96-40cb-8aa0-07619b3158fd,Node,101,con/pcm
	cc114908-cc0f-4680-a92e-718690742ba9,Node,101,gen/1
	```

=== "JSON Output"

	```json
	[ {
	"streamId" : "2e16deac-bd95-4d4b-9668-4441f48aea17",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 64,
	"sourceId" : "/meter/1"
	}, {
	"streamId" : "a73f4797-d9aa-4496-b96c-04ad934b1550",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 64,
	"sourceId" : "/meter/2"
	}, {
	"streamId" : "2d19d690-2472-480d-afc0-240f830d4cd5",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 70,
	"sourceId" : "meter/1"
	}, {
	"streamId" : "03c6bd01-9241-4771-ad3c-a1d5eb06b68a",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "con/1"
	}, {
	"streamId" : "0c0fc45d-1e96-40cb-8aa0-07619b3158fd",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "con/pcm"
	}, {
	"streamId" : "cc114908-cc0f-4680-a92e-718690742ba9",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "gen/1"
	} ]
	```



[datum-stream-meta]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#datum-stream-metadata
