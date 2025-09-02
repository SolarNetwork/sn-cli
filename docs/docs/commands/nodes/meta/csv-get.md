---
title: csv-get
---
# Nodes Meta CSV-Get

Extract CSV from node metadata at a specific metadata [key path][metadata-key-path]. CSV metadata
is often used for tariff schedules in SolarNode, such as [tariff schedule expressions][tariff-expr].

# Usage

```
s10k nodes meta csv-get [-mode=<displayMode>] -node=<nodeId>
                        [-path=<metadataPath>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to get metadata from |
| `-path=` | `--path=` | the [key path][metadata-key-path] with the CSV data to extract |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

The CSV data, formatted according to the `--display-mode` option.

## Examples

Show CSV data in the default formatting:

=== "Show CSV with pretty formatting"

	```sh
	s10k nodes meta csv-get --node-id 101 --path /pm/cpd/schedule
	```

=== "Output"

	```
	+-----------------+-----+---------+------+-----------------+
	|           Month | Day | Weekday | Time | cpdPeriodActive |
	+-----------------+-----+---------+------+-----------------+
	| Jan-Apr,Oct-Dec |     |         |      |               0 |
	+-----------------+-----+---------+------+-----------------+
	|         May-Sep |     |         |      |               1 |
	+-----------------+-----+---------+------+-----------------+
	```

The same data can be output as CSV as well:

=== "Show CSV with CSV formatting"

	```sh
	s10k nodes meta csv-get --node-id 101 --path /pm/cpd/schedule \
	     --display-mode CSV
	```

=== "Output"

	```
	Month,Day,Weekday,Time,cpdPeriodActive
	"Jan-Apr,Oct-Dec",,,,0
	May-Sep,,,,1
	```

The same data can be output as JSON as well:

=== "Show CSV with JSON formatting"

	```sh
	s10k nodes meta csv-get --node-id 101 --path /pm/cpd/schedule \
	     --display-mode JSON
	```

=== "Output"

	```
	[
	  [ "Month", "Day", "Weekday", "Time", "cpdPeriodActive" ],
	  [ "Jan-Apr,Oct-Dec", null, null, null, "0" ],
	  [ "May-Sep", null, null, null, "1" ]
	]
	```

[metadata-key-path]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#metadata-filter-key-paths
[tariff-expr]: https://solarnetwork.github.io/solarnode-handbook/users/expressions/#tariff-schedule-functions
