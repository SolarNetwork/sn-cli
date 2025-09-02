---
title: ids
---
# Nodes IDs

List node IDs for the active credentials.

# Usage

```
s10k nodes ids [-mode=<displayMode>]
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
	s10k nodes ids
	```

=== "Pretty Output"

	```
	+---------+
	| Node ID |
	+---------+
	|      64 |
	+---------+
	|      70 |
	+---------+
	|     101 |
	+---------+
	```

=== "CSV Output"

	```csv
	Node ID
	64
	70
	101
	```

=== "JSON Output"

	```json
	[
		64,
		70,
		101
	]
	```
