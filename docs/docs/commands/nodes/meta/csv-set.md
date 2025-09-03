---
title: csv-set
---
# Nodes Meta CSV-Set

Save CSV data to node metadata at a specific metadata [key path][metadata-key-path]. CSV metadata
is often used for tariff schedules in SolarNode, such as [tariff schedule expressions][tariff-expr].

## Usage

```
s10k nodes meta csv-set [-s] [-mode=<displayMode>] -node=<nodeId>
                        [-path=<metadataPath>] [<csv>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to get metadata from |
| `-path=` | `--path=` | the [key path][metadata-key-path] with the CSV data to extract |
| `-mode=` | `--display-mode=` | if `--verbose` then the format to display the saved data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |
| `-s` | `--string` | encode the CSV as a JSON string, intead of JSON arrays |

</div>

## Output

A success message. If the `--verbose` option specified then this will be followed by a
pretty-printed copy of the saved CSV data, formatted according to the `--display-mode` option.

## String mode

The `--string` option lets you encode the CSV data as a metadata string value, rather than
the default method of encoding it as arrays. For example take this sample CSV data:

```csv
Month,Day,Weekday,Time,cpdPeriodActive
"Jan-Apr,Oct-Dec",,,,0
May-Sep,,,,1
```

Here is how it would be saved as metadata at the key path `/pm/cpd/schedule` normally, and with the
`--string` option:

=== "Normal CSV metadata"

	```json
	{
		"pm" : {
			"cpd" : {
      			"schedule" : [
					[ "Month", "Day", "Weekday", "Time", "cpdPeriodActive" ],
					[ "Jan-Apr,Oct-Dec", null, null, null, "0" ],
					[ "May-Sep", null, null, null, "1" ]
				]
			}
		}
	}
	```

=== "String CSV metadata"

	```json
	{
		"pm" : {
			"cpd" : {
				"schedule" : "Month,Day,Weekday,Time,cpdPeriodActive\r\n\"Jan-Apr,Oct-Dec\",,,,0\r\nMay-Sep,,,,1"
			}
		}
	}
	```

## Examples

The data to save can be provided directly as a command argument, for example:

```sh title="CSV as command argument"
s10k nodes meta csv-set --node-id 101 \
  --path /pm/cpd/schedule 'Key,Value\nA,B'
```

A file with the data can be referenced using `@@` followed by the file path:

```sh title="CSV as a file"
s10k nodes meta csv-set --node-id 101 \
  --path /pm/cpd/schedule @@/path/to/data.csv
```

The data can be read from standard input, like this:

```sh title="CSV read from standard input"
s10k nodes meta csv-set --node-id 101 \
  --path /pm/cpd/schedule </path/to/data.csv
```

Similarly, the metadata content can be piped to the command, like this:

=== "Example"

	```sh title="Metadata piped from standard input"
	cat /path/to/data.csv |s10k --verbose --profile demo nodes meta csv-set \
	  --node-id 101 --path /pm/cpd/schedule --display-mode JSON
	```

=== "Output"

	```
	CSV saved to to path [/pm/cpd/schedule].
	[
	  [ "Month", "Day", "Weekday", "Time", "cpdPeriodActive" ],
	  [ "Jan-Apr,Oct-Dec", null, null, null, "0" ],
	  [ "May-Sep", null, null, null, "1" ]
	]
	```

[metadata-key-path]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#metadata-filter-key-paths
[tariff-expr]: https://solarnetwork.github.io/solarnode-handbook/users/expressions/#tariff-schedule-functions
