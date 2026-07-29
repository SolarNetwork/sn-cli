---
title: ids
---
# Datum Delete IDs

Delete a small number of datum by their identifiers.

!!! note

	This command can be used to delete up to 100 datum. Use the [range](./range.md) command
	to delete any number of datum based on a time range.

## Usage

```
s10k datum delete ids
	[-I]
	[-stream=streamDatumKey[,streamDatumKey...]]...
	[-node=nodeDatumKey[,nodeDatumKey...]]...
	[-tz=<zone>]
    [-mode=<displayMode>]
	[<identifiers>]
```

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to preview the deletion,
	without actually changing anything. For example:

	```sh
	s10k --dry-run datum delete ids --node-datum '10:/GEN/1:2026-01-01 12:00:00'
	```


## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON settings object |
| `-node=` | `--node-datum=` | a node datum identifer to delete, in the form `nodeId:sourceId:timestamp` |
| `-stream=` | `--stream-datum=` | a stream datum identifier to delete, in the form `streamId:timestamp` |
| `-tz=` | `--time-zone=` | a time zone ID to interpret option timestamps as, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

### Datum identifier values

The `--node-datum` and `--stream-datum` options allow you to specify specific datum to delete
directly. The associated option value is a colon-delimited datum identifier in the form
`nodeId:sourceId:timestamp` and `streamId:timestamp`, repsectively. The `timestamp` portion of the
identifier is an ISO 8601 local timestamp, interpreted in the system default time zone or the zone
specified with the `--time-zone` option. Either a space or `T` character can be used to delimit the
date and time portions of the timestamp. Both options can be freely mixed and matched and provided
multiple times or as a comma-delimited list.

For example `--node-datum 123:/my/device:2026-01-01T08:10:01` would delete the datum for node
`123` source `/my/device` at `2026-01-01T08:10:01`.

For another example, `--stream-datum 82c6be47-0000-0000-0000-027a21fdeaba:2026-01-01T08:10:01` would
delete the datum for stream ID `82c6be47-0000-0000-0000-027a21fdeaba` at `2026-01-01T08:10:01`

## Input

You can provide a JSON array of datum identifier objects to delete with:

 1. Standard input, as a JSON object
 2. Command line parameter JSON object, or `@@` file reference

Each identifier object must provide a `kind` property with the value `n` (for node), and the
identify of a single datum by providing **either**:

 1. `objectId`, `sourceId`, and `timestamp` properties
 2. `streamId` and `timestamp` properties

The `timestamp` property must be a full ISO 8601 timestamp, including a time zone (the `--time-zone`
option does not apply to input identifiers).

For example:

```json title="Datum identifier input"
[
	{ "kind": "n", "objectId": 123, "sourceId": "/my/device", "timestamp": "2025-01-02 05:09:26Z" },
	{ "kind": "n", "streamId": "82c6be47-0000-0000-0000-027a21fdeaba", "timestamp": "2025-01-02 05:09:26Z" }
]
```

The following invocations produce equivalent results:

```sh
# using standard input
echo '{"kind":"n","objectId":123,"sourceId":"/my/device","timestamp":"2025-01-02 05:09:26Z"}' \
    |s10k datum delete ids

# using parameter value
s10k datum delete ids \
    '{"kind":"n","objectId":123,"sourceId":"/my/device","timestamp":"2025-01-02 05:09:26Z"}'

# using parameter file reference - my-file.json contains
# '{"kind":"n","objectId":123,"sourceId":"/my/device","timestamp":"2025-01-02 05:09:26Z"}'
s10k datum delete ids @@my-file.json

# using options
s10k datum delete ids --node-datum '123:/my/device:2026-01-01 18:09:26'
```

## Output

The identifiers of the deleted datum (or a preview of the deletion if the `--dry-run` option was given).

## Examples

=== "Delete datum by node/source ID"

	```sh
	s10k datum imports ids --node-datum '10:/GEN/1:2026-01-01 12:00:00'
	```

=== "Preview staged datum import (shortcut)"

	You can use `del` instead of `delete`:

	```sh
	s10k datum del ids --node-datum '10:/G2/S2/S1/GEN/1:2025-01-02 18:03:26.001'
	```

=== "Pretty Output"

	```
	+------+--------------------------------------+-----------+-----------------+-------------------------------+
	| Kind | Stream ID                            | Object ID | Source ID       | Timestamp                     |
	+------+--------------------------------------+-----------+-----------------+-------------------------------+
	| Node | 82c6be47-0000-0000-0000-027a21fdeaba |        10 | /G2/S2/S1/GEN/1 | 2025-01-02 18:03:26.001+13:00 |
	+------+--------------------------------------+-----------+-----------------+-------------------------------+
	```

=== "CSV Output"

	```csv
	Kind,Stream ID,Object ID,Source ID,Timestamp
	Node,82c6be47-0000-0000-0000-027a21fdeaba,10,/G2/S2/S1/GEN/1,2025-01-02 18:03:26.001+13:00
	```

=== "JSON Output"

	```json
	[
	  {
	    "kind": "n",
	    "streamId": "82c6be47-0000-0000-0000-027a21fdeaba",
	    "objectId": 10,
	    "sourceId": "/G2/S2/S1/GEN/1",
	    "timestamp": "2025-01-02 05:03:26.001Z",
	    "aggregation": "None"
	  }
	]
	```

### Generate identifiers with query

You can generate a list of datum identifiers to delete using the [datum list](../list.md) command:

```sh title="Generate identifiers to delete"
s10k datum list --node-id 10 --source-id /G2/S2/S1/GEN/1 \
    --min-date 2025-01-01 --max-date 2025-02-01 --local-dates \
	--max 10 --expand-json -mode json \
	|jq 'map({timestamp:.created, kind:"n", objectId:.nodeId, sourceId:.sourceId})' \
	|s10k datum delete ids
```
