---
title: list
---
# Cloud Datum Stream Poll Task List

Show [Cloud Datum Stream Poll Task][poll-task] entities matching a search filter.

## Usage

```
s10k cloud-integrations datum-streams poll-tasks list
	[-stream=datumStreamId[,datumStreamId...]]...
	[-node=nodeId[,nodeId...]]...
	[-source=sourceId[,sourceId...]]...
	[-state=jobState[,jobState...]]...
	[-M=max]
	[-O=<resultOffset>]
	[-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the datum stream ID(s) to show tasks for |
| `-node=` | `--node-id=` | the node ID of datum streams to show tasks for (matching against datum stream `objectId` values) |
| `-source=` | `--source=` | the source ID(s) to match; will match mapped and virtual source IDs and wildcard patterns are supported |
| `-state=` | `--job-state=` | the [claimable job state][job-states] names to show tasks for, for example `Queued` or `Completed` |
| `-M=` | `--max=` | the maximum number of results to return |
| `-O=` | `--offset=` | start returning results from this offset, `0` being the first result |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of matching poll tasks.

## Examples

=== "List poll tasks"

	```sh
	s10k cloud-integrations datum-streams poll-tasks list
	```

=== "List poll tasks (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams` and `polls` instead of `poll-tasks`:

	```sh
	s10k c2c ds polls list
	```

=== "Pretty Output"

	```
	+-----------------+-------+----------------------+----------------------+--------------------------------------------------------------------------------+
	| Datum Stream ID | State | Execute At           | Start At             | Message                                                                        |
	+-----------------+-------+----------------------+----------------------+--------------------------------------------------------------------------------+
	|            1000 | q     | 2026-06-26T07:00:00Z | 2026-06-26T06:45:00Z |                                                                                |
	+-----------------+-------+----------------------+----------------------+--------------------------------------------------------------------------------+
	|            1001 | q     | 2026-06-26T07:01:00Z | 2026-06-17T20:45:00Z | Error executing poll task. 429 Too Many Requests on GET request for            |
	|                 |       |                      |                      | "https://api.enphaseenergy.com/api/v4/systems/0000000/telemetry/production_mic |
	|                 |       |                      |                      | ro": "{"message":"Too Many Requests","details":"Usage limit exceeded for plan  |
	|                 |       |                      |                      | Kilowatt","code":429}"                                                         |
	+-----------------+-------+----------------------+----------------------+--------------------------------------------------------------------------------+
	```

=== "CSV Output"

	```csv
	Datum Stream ID,State,Execute At,Start At,Message
	1000,q,2026-06-26T07:30:00Z,2026-06-26T07:15:00Z,
	1001,q,2026-06-26T07:01:00Z,2026-06-17T20:45:00Z,"Error executing poll task. 429 Too Many Requests on GET request for ""https://api.enphaseenergy.com/api/v4/systems/0000000/telemetry/production_micro"": ""{""message"":""Too Many Requests"",""details"":""Usage limit exceeded for plan Kilowatt"",""code"":429}"""
	```

=== "JSON Output"

	```json
	[
		{
			"datumStreamId": 1000,
			"state": "q",
			"executeAt": "2026-06-26 07:30:00Z",
			"startAt": "2026-06-26 07:15:00Z"
		},
		{
			"datumStreamId": 1001,
			"state": "q",
			"executeAt": "2026-06-26 07:01:00Z",
			"startAt": "2026-06-17 20:45:00Z",
			"message": "Error executing poll task.",
			"serviceProperties": {
				"message": "429 Too Many Requests on GET request for \"https://api.enphaseenergy.com/api/v4/systems/0000000/telemetry/production_micro\": \"{\"message\":\"Too Many Requests\",\"details\":\"Usage limit exceeded for plan Kilowatt\",\"code\":429}\"",
				"errorCount": 16
			}
		}
	]
	```

[job-states]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#claimable-job-state-type
[poll-task]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-poll-task
