---
title: create
---
# Cloud Datum Stream Rake Task Create

Create [Cloud Datum Stream Rake Task][rake-task] entities.

## Usage

```
s10k cloud-integrations datum-streams rake-tasks create
	[-stream=datumStreamId[,datumStreamId...]]...
	[-o=offset[,offset...]]...
    [-t=datumStreamType[,datumStreamType...]]...
	[-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the datum stream ID(s) to create tasks for; if unspecified then create tasks for all available datum streams |
| `-o=` | `--offset=` | a rake offset, in the form of an ISO 8601 period, for example `P3D` for 3 days |
| `-t=` | `--stream-type=` | a datum stream service identifier filter; a case-insensitive sub-string match is performed against both the service identifier and the display name, for example `also` will match the AlsoEnergy type; prefix with a `!` character to **exclude** streams matching that type |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../../global-options.md) to generate a report of what tasks would be
	created or removed, without actually changing anything. For example:

	```sh
	# dry run to create 3- and 7-day rake tasks for all datum streams except for those
	# using Solcast and OpenWeatherMap integrations
	s10k --dry-run cloud-integrations datum-streams rake-tasks create
	  --stream-type !solcast,!weather
	  --offset P3D,P7D
	```

## Output

A listing of matching rake tasks.

## Examples

=== "Create rake tasks"

	```sh
	s10k --dry-run cloud-integrations datum-streams rake-tasks create --stream-id 1000
	```

=== "Create rake tasks (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams` and `rakes` instead of `rake-tasks`:

	```sh
	s10k c2c ds rakes create --stream-id 1000 --offset P3D,P7D
	```

=== "Pretty Output"

	```
	+-----------------+-------------------+-----------+---------------------+--------+----------------------+--------+
	| Datum Stream ID | Datum Stream Type | Object ID | Source ID           | Offset | Execute At           | Action |
	+-----------------+-------------------+-----------+---------------------+--------+----------------------+--------+
	|            1000 | eGauge            |       123 | /S1F2/S1/B1/GEN/100 | P3D    | 2026-06-29T04:00:00Z | Create |
	|                 |                   |           | /S1F2/S1/B1/INV/1   |        |                      |        |
	+-----------------+-------------------+-----------+---------------------+--------+----------------------+--------+
	|            1000 | eGauge            |       123 | /S1F2/S1/B1/GEN/100 | P7D    | 2026-06-29T04:00:00Z | Create |
	|                 |                   |           | /S1F2/S1/B1/INV/1   |        |                      |        |
	+-----------------+-------------------+-----------+---------------------+--------+----------------------+--------+
	|            1000 | eGauge            |       123 | /S1F2/S1/B1/GEN/100 | P5D    | 2026-06-29T04:00:00Z | Remove |
	|                 |                   |           | /S1F2/S1/B1/INV/1   |        |                      |        |
	+-----------------+-------------------+-----------+---------------------+--------+----------------------+--------+
	```

=== "CSV Output"

	```csv
	Datum Stream ID,Datum Stream Type,Object ID,Source ID,Offset,Execute At,Action
	1000,eGauge,123,"/S1F2/S1/B1/100
	/S1F2/S1/B1/INV/1",P3D,2026-06-29T04:00:00Z,Create
	1000,eGauge,123,"/S1F2/S1/B1/GEN/100
	/S1F2/S1/B1/INV/1",P7D,2026-06-29T04:00:00Z,Create
	1000,eGauge,123,"/S1F2/S1/B1/GEN/100
	/S1F2/S1/B1/INV/1",P5D,2026-06-29T04:00:00Z,Remove
	```

=== "JSON Output"

	```json
	{
		"1000" : {
			"datumStreamId" : 1000,
			"missingOffsets" : [ "P3D", "P7D" ],
			"undesiredOffsets" : {
				"P5D" : {
					"configId" : 111,
					"datumStreamId" : 1000,
					"state" : "q",
					"executeAt" : "2026-06-29 04:00:00Z",
					"offset" : "P5D"
				}
			}
		}
	}
	```

[rake-task]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-rake-task
