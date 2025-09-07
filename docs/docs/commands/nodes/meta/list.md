---
title: list
---
# Nodes Meta List

Show metadata matching a search filter.

## Usage

```
s10k nodes meta list [-filter=<filter>] -node=nodeId[,nodeId...]
                     [-node=nodeId[,nodeId...]]...
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID(s) to show metadata for |
| `-filter=` | `--filter=` | an optional [metadata filter][metadata-filter] to limit results to |

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
	Property Value
	------------------------------------
	nodeId   101
	created  2025-08-28T23:22:00.237150Z
	updated  2025-08-29T05:20:06.322691Z
	{
		"m" : {
			"limit" : 123,
		}
	}
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
	Property Value
	------------------------------------
	nodeId   101
	created  2025-08-28T23:22:00.237150Z
	updated  2025-08-29T05:20:06.322691Z
	{
		"m" : {
			"limit" : 123
		}
	}

	Property Value
	------------------------------------
	nodeId   102
	created  2025-08-28T12:22:00.433152Z
	updated  2025-08-28T18:11:21.627128Z
	{
		"m" : {
			"limit" : 234
		}
	}
	```

You can restrict the returned metadata using a [metadata filter][metadata-filter], for example
to show only the metadata where `limit` is greater than `200`:

=== "Show metadata for one node"

	```sh
	s10k nodes meta list --node-id 101,102 --filter '(m/limit>200)'
	```

=== "Output"

	```
	Property Value
	------------------------------------
	nodeId   102
	created  2025-08-28T12:22:00.433152Z
	updated  2025-08-28T18:11:21.627128Z
	{
		"m" : {
			"limit" : 234
		}
	}
	```


[metadata-filter]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#metadata-filter
