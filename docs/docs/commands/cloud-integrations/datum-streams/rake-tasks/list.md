---
title: list
---
# Cloud Datum Stream Rake Task List

Show [Cloud Datum Stream Rake Task][rake-task] entities matching a search filter.

## Usage

```
s10k cloud-integrations datum-streams rake-tasks list
	[-stream=datumStreamId[,datumStreamId...]]...
	[-task=rakeTaskId[,rakeTaskId...]]...
	[-node=nodeId[,nodeId...]]...
	[-state=jobState[,jobState...]]...
	[-M=max]
	[-O=<resultOffset>]
	[-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the datum stream ID(s) to show tasks for |
| `-task=` | `--task-id=` | the rake task ID(s) to show |
| `-node=` | `--node-id=` | the node ID of datum streams to show tasks for (matching against datum stream `objectId` values) |
| `-state=` | `--job-state=` | the [claimable job state][job-states] names to show tasks for, for example `Queued` or `Completed` |
| `-M=` | `--max=` | the maximum number of results to return |
| `-O=` | `--offset=` | start returning results from this offset, `0` being the first result |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of matching rake tasks.

## Examples

=== "List rake tasks"

	```sh
	s10k cloud-integrations datum-streams rake-tasks list --stream-id 1000
	```

=== "List rake tasks (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams` and `rakes` instead of `rake-tasks`:

	```sh
	s10k c2c ds rakes list --stream-id 1000
	```

=== "Pretty Output"

	```
	+---------+-----------------+-------+----------------------+--------+-------------------+
	| Task ID | Datum Stream ID | State | Execute At           | Offset | Message           |
	+---------+-----------------+-------+----------------------+--------+-------------------+
	|     100 |            1000 | q     | 2026-06-27T04:00:00Z | P3D    |                   |
	+---------+-----------------+-------+----------------------+--------+-------------------+
	|     101 |            1000 | q     | 2026-06-27T04:00:00Z | P7D    |                   |
	+---------+-----------------+-------+----------------------+--------+-------------------+
	|     102 |            1000 | q     | 2026-06-27T04:00:00Z | P14D   | Updated 39 datum. |
	+---------+-----------------+-------+----------------------+--------+-------------------+
	|     103 |            1000 | q     | 2026-06-26T04:00:00Z | P21D   |                   |
	+---------+-----------------+-------+----------------------+--------+-------------------+
	```

=== "CSV Output"

	```csv
	Task ID,Datum Stream ID,State,Execute At,Offset,Message
	100,1000,q,2026-06-27T04:00:00Z,P3D,
	101,1000,q,2026-06-27T04:00:00Z,P7D,
	102,1000,q,2026-06-27T04:00:00Z,P14D,Updated 39 datum.
	103,1000,q,2026-06-26T04:00:00Z,P21D,
	```

=== "JSON Output"

	```json
	[
		{
			"configId": 100,
			"datumStreamId": 1000,
			"state": "q",
			"executeAt": "2026-06-27 04:00:00Z",
			"offset": "P3D"
		},
		{
			"configId": 101,
			"datumStreamId": 1000,
			"state": "q",
			"executeAt": "2026-06-27 04:00:00Z",
			"offset": "P7D"
		},
		{
			"configId": 102,
			"datumStreamId": 1000,
			"state": "q",
			"executeAt": "2026-06-27 04:00:00Z",
			"offset": "P14D",
			"message": "Updated 39 datum."
		},
		{
			"configId": 103,
			"datumStreamId": 1000,
			"state": "q",
			"executeAt": "2026-06-26 04:00:00Z",
			"offset": "P21D"
		}
	]
	```

[rake-task]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-rake-task
