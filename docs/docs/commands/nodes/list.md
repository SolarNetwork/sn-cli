---
title: list
---
# Nodes List

List node information for the active credentials.

## Usage

```
s10k nodes list [-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of all available nodes.

## Examples

=== "List nodes"

	```sh
	s10k nodes list
	```

=== "Pretty Output"

	```
	+---------+-----------------------------+--------+---------+------------------+
	| Node ID | Created                     | Public | Country | Time Zone        |
	+---------+-----------------------------+--------+---------+------------------+
	|      64 | 2023-04-27T23:15:58.795135Z | true   | NZ      | Pacific/Auckland |
	+---------+-----------------------------+--------+---------+------------------+
	|      70 | 2024-05-16T19:02:54.174785Z | true   | NZ      | Pacific/Auckland |
	+---------+-----------------------------+--------+---------+------------------+
	|     101 | 2025-07-08T02:21:37.083163Z | true   | NZ      | Pacific/Auckland |
	+---------+-----------------------------+--------+---------+------------------+
	```

=== "CSV Output"

	```csv
	Node ID,Created,Public,Country,Time Zone
	64,2023-04-27T23:15:58.795135Z,true,NZ,Pacific/Auckland
	70,2024-05-16T19:02:54.174785Z,true,NZ,Pacific/Auckland
	101,2025-07-08T02:21:37.083163Z,true,NZ,Pacific/Auckland
	```

=== "JSON Output"

	```json
	[
		[ 664, "2023-04-27 23:15:58.795135Z", true, "NZ", "Pacific/Auckland" ],
		[ 707, "2024-05-16 19:02:54.174785Z", true, "NZ", "Pacific/Auckland" ],
		[ 1010, "2025-07-08 02:21:37.083163Z", true, "NZ", "Pacific/Auckland" ],
		[ 1011, "2025-07-08 03:23:26.007558Z", true, "NZ", "Pacific/Auckland" ]
	]
	```
