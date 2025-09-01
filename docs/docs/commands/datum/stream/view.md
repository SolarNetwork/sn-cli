---
title: view
---
# Datum Stream View

View a specific [datum stream metadata][datum-stream-meta].

# Usage

```
s10k datum stream view [-mode=<displayMode>] -stream=streamId
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the stream ID to show |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A table of datum stream metadata properties.

## Examples

=== "View datum stream metadata"

	```sh
	s10k --profile demo datum stream view --stream-id cc114908-cc0f-4680-a92e-718690742ba9
	```

=== "Pretty Output"

	```
	+---------------+--------------------------------------+
	| Property      | Value                                |
	+---------------+--------------------------------------+
	| Stream ID     | cc114908-cc0f-4680-a92e-718690742ba9 |
	+---------------+--------------------------------------+
	| Kind          | Node                                 |
	+---------------+--------------------------------------+
	| ID            | 101                                  |
	+---------------+--------------------------------------+
	| Source ID     | gen/1                                |
	+---------------+--------------------------------------+
	| Time Zone     | Pacific/Auckland                     |
	+---------------+--------------------------------------+
	| Instantaneous | watts,current,voltage,frequency      |
	+---------------+--------------------------------------+
	| Accumulating  | wattHours                            |
	+---------------+--------------------------------------+
	| Status        |                                      |
	+---------------+--------------------------------------+
	```

=== "CSV Output"

	```csv
	Stream ID,Kind,ID,Source ID,Time Zone,Instantaneous,Accumulating,Status
	cc114908-cc0f-4680-a92e-718690742ba9,Node,101,gen/1,Pacific/Auckland,"current,frequency,voltage,watts",wattHours,
	```

=== "JSON Output"

	```json
	{
	"streamId" : "cc114908-cc0f-4680-a92e-718690742ba9",
	"zone" : "Pacific/Auckland",
	"kind" : "n",
	"objectId" : 101,
	"sourceId" : "gen/1",
	"i" : [ "watts", "current", "voltage", "frequency" ],
	"a" : [ "wattHours" ]
	}
	```

[datum-stream-meta]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#datum-stream-metadata
