---
title: create
---
# Cloud Datum Stream Poll Task Create

Create [Cloud Datum Stream Rake Task][rake-task] entities.

## Usage

```
s10k cloud-integrations datum-streams poll-tasks create
	[-stream=datumStreamId[,datumStreamId...]]...
	[-s=<startDate>]
    [-tz=<zone>]
	[-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the datum stream ID(s) to create tasks for; if unspecified then create tasks for all available datum streams |
| `-s=` | `--start-date=` | the polling start date (the date to start collecting datum from) |
| `-t=` | `--stream-type=` | a datum stream service identifier filter to create tasks for; a case-insensitive sub-string match is performed against both the service identifier and the display name, for example `also` will match the AlsoEnergy type; prefix with a `!` character to **exclude** streams matching that type |
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

A listing of rake tasks "action" records. Each record's action will be either **Create** or **Remove**.

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
	+-----------------+-------------------+-----------+-------------------+-------+----------------------+
	| Datum Stream ID | Datum Stream Type | Object ID | Source ID         | State | Start At             |
	+-----------------+-------------------+-----------+-------------------+-------+----------------------+
	|            1000 | AlsoEnergy        |       123 | /S1F2/S1/B1/GEN/1 | q     | 2026-06-29T04:10:00Z |
	|                 |                   |           | /S1F2/S1/B1/INV/1 |       |                      |
	+-----------------+-------------------+-----------+-------------------+-------+----------------------+
	```

=== "CSV Output"

	```csv
	Datum Stream ID,Datum Stream Type,Object ID,Source ID,State,Start At
	1000,AlsoEnergy,123,"/S1F2/S1/B1/GEN/1
	/S1F2/S1/B1/INV/1",q,2026-06-29T04:25:00Z
	```

=== "JSON Output"

	```json
	[
		{
			"datumStreamId":1000,
			"state":"q",
			"executeAt":"2026-06-29 04:26:00Z",
			"startAt":"2026-06-29 04:26:00Z"
		}
	]
	```

[rake-task]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-rake-task
