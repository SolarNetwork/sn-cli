---
title: list
---
# Nodes Meta List

Show metadata matching a search filter.

## Usage

```
s10k nodes meta list [-filter=<filter>] -node=nodeId[,nodeId...]
                     [-node=nodeId[,nodeId...]]...
					 [-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID(s) to show metadata for |
| `-filter=` | `--filter=` | an optional [metadata filter][metadata-filter] to limit results to |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of all matching node metadata records.

## Examples

You can show exactly the metadata for a single node like this:

=== "Show metadata for one node"

	```sh
	s10k nodes meta list --node-id 101
	```

=== "Output"

	```
	+---------+-----------------------------+-------------------+
	| Node ID | Updated                     | Metadata          |
	+---------+-----------------------------+-------------------+
	|     101 | 2025-08-30T03:36:06.277076Z | {                 |
	|         |                             |   "m" : {         |
	|         |                             |     "limit" : 123 |
	|         |                             |   }               |
	|         |                             | }                 |
	+---------+-----------------------------+-------------------+
	```

You can show multiple node metadatas by providing multiple `--node-id` options
**or** as a comma-delimited list:

=== "Show metadata for multiple nodes"

	```sh
	# using multiple options
	s10k nodes meta list --node-id 101 --node-id 102

	# or the same thing, using a comma-delimited list
	s10k nodes meta list --node-id 101,102
	```

=== "Output"

	```
	+---------+-----------------------------+-------------------+
	| Node ID | Updated                     | Metadata          |
	+---------+-----------------------------+-------------------+
	|     101 | 2025-08-30T03:36:06.277076Z | {                 |
	|         |                             |   "m" : {         |
	|         |                             |     "limit" : 123 |
	|         |                             |   }               |
	|         |                             | }                 |
	+---------+-----------------------------+-------------------+
	|     102 | 2025-08-30T03:36:06.277076Z | {                 |
	|         |                             |   "m" : {         |
	|         |                             |     "limit" : 234 |
	|         |                             |   }               |
	|         |                             | }                 |
	+---------+-----------------------------+-------------------+
	```

You can restrict the returned metadata using a [metadata filter][metadata-filter], for example
to show only the metadata where `limit` is greater than `200`:

=== "Show metadata for one node"

	```sh
	s10k nodes meta list --node-id 101,102 --filter '(m/limit>200)'
	```

=== "Output"

	```
	+---------+-----------------------------+-------------------+
	| Node ID | Updated                     | Metadata          |
	+---------+-----------------------------+-------------------+
	|     102 | 2025-08-30T03:36:06.277076Z | {                 |
	|         |                             |   "m" : {         |
	|         |                             |     "limit" : 234 |
	|         |                             |   }               |
	|         |                             | }                 |
	+---------+-----------------------------+-------------------+
	```


[metadata-filter]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#metadata-filter
