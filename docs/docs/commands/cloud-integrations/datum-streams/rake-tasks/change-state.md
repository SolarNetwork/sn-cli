---
title: change-state
---
# Cloud Datum Stream Rake Task Change State

Change the runtime state of [Cloud Datum Stream Rake Task][rake-task] entities (enable or disable).

## Usage

```
s10k cloud-integrations datum-streams rake-tasks change-state
	-stream=datumStreamId[,datumStreamId...]
	<desiredState>
```

Pass the desired state as the first (and only) parameter. You can specify the state as any of the following:

| Desired State | Supported Parameter Values |
|:--------------|:---------------------------|
| `Enabled`     | `enabled`, `true`, `yes`, `1` |
| `Disabled`    | anything other than one of the valid `Enabled` values |

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the datum stream ID(s) to create tasks for; if unspecified then create tasks for all available datum streams |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../../global-options.md) to generate a report of what tasks would be
	changed, without actually changing anything. For example:

	```sh
	s10k --dry-run cloud-integrations datum-streams rake-tasks change-state --stream-id 100
	```


## Output

A listing of updated rake tasks.

## Examples

=== "Change rake task state"

	```sh
	s10k cloud-integrations datum-streams rake-tasks change-state --stream-id 100
	```

=== "Change rake task state (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams` and `rakes` instead of `rake-tasks`:

	```sh
	s10k c2c ds rakes change-state --stream-id 100 enabled
	```

=== "Pretty Output"

	```
	+-----------+---------+---------+------------+------+-----------+-------------------+----------------+----------------------+--------+-----------+-----------+
	| Stream ID | Task ID | Name    | Type       | Kind | Object ID | Source ID         | Schedule       | Execute At           | Offset | Old State | New State |
	+-----------+---------+---------+------------+------+-----------+-------------------+----------------+----------------------+--------+-----------+-----------+
	|       100 |     392 | My Site | SolrenView | n    |       123 | /BLD1/S1/R1/GEN/1 | 0 0/30 * * * * | 2026-07-19T04:00:00Z | P3D    | Enabled   | Disabled  |
	|           |         |         |            |      |           | /BLD1/S1/R1/INV/1 |                |                      |        |           |           |
	|           |         |         |            |      |           | /BLD1/S1/R1/INV/2 |                |                      |        |           |           |
	|           |         |         |            |      |           | /BLD1/S1/R1/INV/3 |                |                      |        |           |           |
	+-----------+---------+---------+------------+------+-----------+-------------------+----------------+----------------------+--------+-----------+-----------+
	|       100 |     403 | My Site | SolrenView | n    |       123 | /BLD1/S1/R1/GEN/1 | 0 0/30 * * * * | 2026-07-19T04:00:00Z | P7D    | Enabled   | Disabled  |
	|           |         |         |            |      |           | /BLD1/S1/R1/INV/1 |                |                      |        |           |           |
	|           |         |         |            |      |           | /BLD1/S1/R1/INV/2 |                |                      |        |           |           |
	|           |         |         |            |      |           | /BLD1/S1/R1/INV/3 |                |                      |        |           |           |
	+-----------+---------+---------+------------+------+-----------+-------------------+----------------+----------------------+--------+-----------+-----------+
	```

=== "CSV Output"

	```csv
	Stream ID,Task ID,Name,Type,Kind,Object ID,Source ID,Schedule,Execute At,Offset,Old State,New State
	100,392,My Site,SolrenView,n,123,"/BLD1/S1/R1/GEN/1
	/BLD1/S1/R1/INV/1
	/BLD1/S1/R1/INV/2
	/BLD1/S1/R1/INV/3",0 0/30 * * * *,2026-07-19T04:00:00Z,P3D,Enabled,Disabled
	100,403,My Site,SolrenView,n,123,"/BLD1/S1/R1/GEN/1
	/BLD1/S1/R1/INV/1
	/BLD1/S1/R1/INV/2
	/BLD1/S1/R1/INV/3",0 0/30 * * * *,2026-07-19T04:00:00Z,P7D,Enabled,Disabled
	```

=== "JSON Output"

	Note that in JSON display mode only the rake task state details are returned.

	```json
	[
		{
		"configId" : 392,
		"datumStreamId" : 100,
		"state" : "q",
		"executeAt" : "2026-07-19 04:00:00Z",
		"offset" : "P3D"
		},
		{
		"configId" : 403,
		"datumStreamId" : 100,
		"state" : "q",
		"executeAt" : "2026-07-19 04:00:00Z",
		"offset" : "P7D"
		}
	]
	```

[rake-task]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-rake-task
