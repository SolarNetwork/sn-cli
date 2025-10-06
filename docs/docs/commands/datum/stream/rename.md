---
title: rename
---
# Datum Stream Rename

Rename node [datum stream metadata][datum-stream-meta] attributes, such as the
node ID, source ID, and property names.

## Usage

```
s10k datum stream rename -stream=<streamId>
						[-node=<nodeId>]
						[-source=<sourceId>]
						[-i=propName[,propName...]]...
						[-a=propName[,propName...]]...
						[-s=propName[,propName...]]...
						[-mode=<displayMode>]
```

!!! warning "About property names"

	The **order** of the property names is significant, and corresponds to the property _values_
	within the datum stream. You can **rename** the property names, but you can **not reorder**
	them. Use the `--unordered-names` option when using the [datum stream list](./list.md) command
	to preserve the physical property name order.

	You can **add** new property names at any time, but you can not remove names. When updating
	any of the property name lists you must provide at least as many names as currently exists
	on the stream.

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the stream ID to update |
| `-node=` | `--node-id=` | the node ID to set, or omit to leave unchanged; the new ID must be owned by the same account as the current ID |
| `-source=` | `--source=` | the source ID to set, or omit to leave unchanged |
| `-i=` | `--instantaneous=` | the **instantaneous** property names to set, or omit to leave unchanged |
| `-a=` | `--accumulating=` | the **accumulating** property names to set, or omit to leave unahanged |
| `-s=` | `--status=` | the **status** property names to set, or omit to leave unahanged |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

The updated stream metadata.

## Examples

Update the source ID of a datum stream:

=== "Update source ID"

	```sh
	s10k datum stream rename --stream-id f68e81cd-2ac3-4760-b449-16ebce64c15a \
		--source-id RELAY/1
	```

=== "Pretty Output"

	```
	+--------------------------------------+------+-----+---------------+------------------+---------------------------------+--------------+--------+
	| Stream ID                            | Kind | ID  | Source ID     | Time Zone        | Instantaneous                   | Accumulating | Status |
	+--------------------------------------+------+-----+---------------+------------------+---------------------------------+--------------+--------+
	| f68e81cd-2ac3-4760-b449-16ebce64c15a | Node | 101 | RELAY/1       | Pacific/Auckland |                                 |              | val    |
	+--------------------------------------+------+-----+---------------+------------------+---------------------------------+--------------+--------+
	```

=== "CSV Output"

	```csv
	Stream ID,Kind,ID,Source ID,Time Zone,Instantaneous,Accumulating,Status
	f68e81cd-2ac3-4760-b449-16ebce64c15a,Node,101,RELAY/1,Pacific/Auckland,,,val
	```

=== "JSON Output"

	```json
	{
		"streamId" : "f68e81cd-2ac3-4760-b449-16ebce64c15a",
		"zone" : "Pacific/Auckland",
		"kind" : "n",
		"objectId" : 101,
		"sourceId" : "RELAY/1",
		"s" : [ "val" ]
	}
	```

If you want to rename a property, you must provide all property names of that class.
For example:

=== "Rename instantaneous properties"

	```sh
	s10k datum stream rename --stream-id cc114908-cc0f-4680-a92e-718690742ba9 \
		--instantaneous watts,amps,voltage,hertz
	```

=== "Pretty Output"

	```
	+--------------------------------------+------+-----+-----------+------------------+------------------------------------------+--------------+--------+
	| Stream ID                            | Kind | ID  | Source ID | Time Zone        | Instantaneous                            | Accumulating | Status |
	+--------------------------------------+------+-----+-----------+------------------+------------------------------------------+--------------+--------+
	| cc114908-cc0f-4680-a92e-718690742ba9 | Node | 101 | gen/1     | Pacific/Auckland | watts,amps,voltage,hertz                 | wattHours    |        |
	+--------------------------------------+------+-----+-----------+------------------+------------------------------------------+--------------+--------+
	```

=== "CSV Output"

	```csv
	Stream ID,Kind,ID,Source ID,Time Zone,Instantaneous,Accumulating,Status
	cc114908-cc0f-4680-a92e-718690742ba9,Node,101,gen/1,Pacific/Auckland,"watts,amps,voltage,hertz",wattHours,
	```

=== "JSON Output"

	```json
	{
		"streamId" : "cc114908-cc0f-4680-a92e-718690742ba9",
		"zone" : "Pacific/Auckland",
		"kind" : "n",
		"objectId" : 101,
		"sourceId" : "gen/1",
		"i" : [ "watts", "amps", "voltage", "hertz" ],
		"a" : [ "wattHours" ]
	}
	```


[datum-stream-meta]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#datum-stream-metadata
