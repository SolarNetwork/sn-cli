---
title: report
---
# Cloud Datum Stream Report

Generate an operational report on [Cloud Datum Stream][datum-stream] poll and rake task status.

## Usage

```
s10k cloud-integrations datum-streams list [-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A report with several sections on the operational status of datum streams will be generated:

| Section | Description |
|:--------|:------------|
| **Overall** | Total number of warnings found overall, for poll tasks, and for rake tasks. |
| **Missing poll tasks** | List of datum streams that have no associated poll task configured. |
| **Missing rake tasks** | List of datum streams that have no associated rake tasks configured. |
| **Poll Tasks: stopped** | List of poll tasks in the `Completed` state, meaning they are not scheduled to run. |
| **Poll Tasks: failing** | List of poll tasks that are still running, but have failed to acquire datum due to an error. |
| **Poll Tasks: lagging** | List of rake tasks that are still running, but the point in time they are acquiring datum at is lagging behind the current date. |
| **Rake Tasks: stopped** | List of rake tasks in the `Completed` state, meaning they are not scheduled to run. |
| **Rake Tasks: failing** | List of rake tasks that are still running, but have failed to acquire datum due to an error. |
| **Rake Tasks: lagging** | List of rake tasks that are still running, but the point in time they are acquiring datum at is lagging behind the current date. |

If no warnings are found, no output will be generated and a message to that effect will be output instead.

### Report file output

The `--directory` option can be used to save the report output into files. The files generated
depend on the `--display-mode` option.


=== "CSV/PRETTY file output"

	Individual files will be generated for each report section with warnings, and will be named like the following
	table, using `.csv` or `.txt` file name extensions based on the mode:

	| Section | File name |
	|:--------|:----------|
	| **Overall** | `datum-stream-overall-report.*` |
	| **Missing poll tasks** | `datum-stream-poll-task-missing-report.*` |
	| **Missing rake tasks** | `datum-stream-rake-task-missing-report.*` |
	| **Poll Tasks: stopped** | `datum-stream-poll-task-stopped-report.*` |
	| **Poll Tasks: failing** | `datum-stream-poll-task-failing-report.*` |
	| **Poll Tasks: lagging** | `datum-stream-poll-task-lagging-report.*` |
	| **Rake Tasks: stopped** | `datum-stream-rake-task-stopped-report.*` |
	| **Rake Tasks: failing** | `datum-stream-rake-task-failing-report.*` |
	| **Rake Tasks: lagging** | `datum-stream-rake-task-lagging-report.*` |

=== "JSON file output"

	A single JSON file named `datum-stream-report.json` will be generated that contains all report sections
	with warnings.

## Examples

=== "Generate datum stream report"

	```sh
	s10k cloud-integrations datum-streams report
	```

=== "Generate datum stream report (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams`:

	```sh
	s10k c2c ds report
	```

=== "Pretty Output"

	```
	+--------------------+---------------+-----------------+--------------------+-----------------+--------------------+
	| Datum Stream Count | Warning Count | Poll Task Count | Poll Warning Count | Rake Task Count | Rake Warning Count |
	+--------------------+---------------+-----------------+--------------------+-----------------+--------------------+
	|                504 |            13 |             504 |                  6 |             173 |                  7 |
	+--------------------+---------------+-----------------+--------------------+-----------------+--------------------+

	Datum Streams Missing Rake Task:
	+-----------------+--------------------------+-----------+---------------------+---------+------+----------------+------------+
	| Datum Stream ID | Datum Stream Type        | Object ID | Source ID           | Enabled | Kind | Schedule       | Mapping ID |
	+-----------------+--------------------------+-----------+---------------------+---------+------+----------------+------------+
	|            1004 | eGauge                   |       456 | /S1F2/S1/B4/GEN/100 | true    | n    | 0 0/30 * * * * |        500 |
	|                 |                          |           | /S1F2/S1/B4/INV/1   |         |      |                |            |
	+-----------------+--------------------------+-----------+---------------------+---------+------+----------------+------------+

	Poll Tasks Stopped:
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+
	| Datum Stream ID | Datum Stream Type | Object ID | Source ID           | State | Error Count | State                       | Start At             | Message                                                                        |
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+
	|            1000 | Enphase           |       123 | /S1F2/S1/B1/GEN/100 | c     |        7200 | 2026-06-28T17:50:21.376540Z | 2026-06-18T00:15:00Z | Error executing poll task. 429 Too Many Requests on GET request for            |
	|                 |                   |           | /S1F2/S1/B1/INV/1   |       |             |                             |                      | "https://api.enphaseenergy.com/api/v4/systems/4000000/telemetry/production_mic |
	|                 |                   |           |                     |       |             |                             |                      | ro": "{"message":"Too Many Requests","details":"Usage limit exceeded for plan  |
	|                 |                   |           |                     |       |             |                             |                      | Kilowatt","code":429}"                                                         |
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+
	|            1001 | Enphase           |       234 | /S1F2/S1/B2/GEN/100 | c     |        7200 | 2026-06-28T16:55:40.569604Z | 2026-06-18T00:15:00Z | Error executing poll task. 429 Too Many Requests on GET request for            |
	|                 |                   |           | /S1F2/S1/B2/INV/1   |       |             |                             |                      | "https://api.enphaseenergy.com/api/v4/systems/4000001/telemetry/production_mic |
	|                 |                   |           |                     |       |             |                             |                      | ro": "{"message":"Too Many Requests","details":"Usage limit exceeded for plan  |
	|                 |                   |           |                     |       |             |                             |                      | Kilowatt","code":429}"                                                         |
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+

	Poll Tasks Failing:
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+
	| Datum Stream ID | Datum Stream Type | Object ID | Source ID           | State | Error Count | State                       | Start At             | Message                                                                        |
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+
	|            1002 | Enphase           |       345 | /S1F2/S1/B3/GEN/100 | q     |        7134 | 2026-06-28T19:25:24.609142Z | 2026-06-18T02:45:00Z | Error executing poll task. 429 Too Many Requests on GET request for            |
	|                 |                   |           | /S1F2/S1/B3/INV/1   |       |             |                             |                      | "https://api.enphaseenergy.com/api/v4/systems/4000002/telemetry/production_mic |
	|                 |                   |           |                     |       |             |                             |                      | ro": "{"message":"Too Many Requests","details":"Usage limit exceeded for plan  |
	|                 |                   |           |                     |       |             |                             |                      | Kilowatt","code":429}"                                                         |
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+

	Poll Tasks Lagging:
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+
	| Datum Stream ID | Datum Stream Type | Object ID | Source ID           | State | Error Count | State                       | Start At             | Message                                                                        |
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+
	|            1000 | Enphase           |       123 | /S1F2/S1/B1/GEN/100 | c     |        7200 | 2026-06-28T17:50:21.376540Z | 2026-06-18T00:15:00Z | Error executing poll task. 429 Too Many Requests on GET request for            |
	|                 |                   |           | /S1F2/S1/B1/INV/1   |       |             |                             |                      | "https://api.enphaseenergy.com/api/v4/systems/4000000/telemetry/production_mic |
	|                 |                   |           |                     |       |             |                             |                      | ro": "{"message":"Too Many Requests","details":"Usage limit exceeded for plan  |
	|                 |                   |           |                     |       |             |                             |                      | Kilowatt","code":429}"                                                         |
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+
	|            1001 | Enphase           |       234 | /S1F2/S1/B2/GEN/100 | c     |        7200 | 2026-06-28T16:55:40.569604Z | 2026-06-18T00:15:00Z | Error executing poll task. 429 Too Many Requests on GET request for            |
	|                 |                   |           | /S1F2/S1/B2/INV/1   |       |             |                             |                      | "https://api.enphaseenergy.com/api/v4/systems/4000001/telemetry/production_mic |
	|                 |                   |           |                     |       |             |                             |                      | ro": "{"message":"Too Many Requests","details":"Usage limit exceeded for plan  |
	|                 |                   |           |                     |       |             |                             |                      | Kilowatt","code":429}"                                                         |
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+
	|            1002 | Enphase           |       345 | /S1F2/S1/B3/GEN/100 | q     |        7134 | 2026-06-28T19:25:24.609142Z | 2026-06-18T02:45:00Z | Error executing poll task. 429 Too Many Requests on GET request for            |
	|                 |                   |           | /S1F2/S1/B3/INV/1   |       |             |                             |                      | "https://api.enphaseenergy.com/api/v4/systems/4000002/telemetry/production_mic |
	|                 |                   |           |                     |       |             |                             |                      | ro": "{"message":"Too Many Requests","details":"Usage limit exceeded for plan  |
	|                 |                   |           |                     |       |             |                             |                      | Kilowatt","code":429}"                                                         |
	+-----------------+-------------------+-----------+---------------------+-------+-------------+-----------------------------+----------------------+--------------------------------------------------------------------------------+

	Rake Tasks Stopped:
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	| Datum Stream ID | Datum Stream Type | Object ID | Source ID           | Task ID | State | Error Count | Execute At           | Offset | Message                                                                        |
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	|            1000 | Enphase           |       123 | /S1F2/S1/B1/GEN/100 |     111 | c     |          51 | 2026-06-21T16:00:00Z | P7D    | Error executing rake task. Giving up [List system inverter data] after 1 try;  |
	|                 |                   |           | /S1F2/S1/B1/INV/1   |         |       |             |                      |        | last exception: List system inverter data failed because an invalid HTTP       |
	|                 |                   |           |                     |         |       |             |                      |        | status was returned: 429 TOO_MANY_REQUESTS                                     |
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	|            1001 | Enphase           |       234 | /S1F2/S1/B2/GEN/100 |     222 | c     |          51 | 2026-06-21T16:00:00Z | P7D    | Error executing rake task. Giving up [List system inverter data] after 1 try;  |
	|                 |                   |           | /S1F2/S1/B2/INV/1   |         |       |             |                      |        | last exception: List system inverter data failed because an invalid HTTP       |
	|                 |                   |           |                     |         |       |             |                      |        | status was returned: 429 TOO_MANY_REQUESTS                                     |
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+

	Rake Tasks Failing:
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	| Datum Stream ID | Datum Stream Type | Object ID | Source ID           | Task ID | State | Error Count | Execute At           | Offset | Message                                                                        |
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	|            1002 | Enphase           |       345 | /S1F2/S1/B3/GEN/100 |     333 | q     |          23 | 2026-06-29T04:00:00Z | P7D    | Error executing rake task. Giving up [List system inverter data] after 1 try;  |
	|                 |                   |           | /S1F2/S1/B3/INV/1   |         |       |             |                      |        | last exception: List system inverter data failed because an invalid HTTP       |
	|                 |                   |           |                     |         |       |             |                      |        | status was returned: 429 TOO_MANY_REQUESTS                                     |
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+

	Rake Tasks Lagging:
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	| Datum Stream ID | Datum Stream Type | Object ID | Source ID           | Task ID | State | Error Count | Execute At           | Offset | Message                                                                        |
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	|            1000 | Enphase           |       123 | /S1F2/S1/B1/GEN/100 |     111 | c     |          51 | 2026-06-21T16:00:00Z | P7D    | Error executing rake task. Giving up [List system inverter data] after 1 try;  |
	|                 |                   |           | /S1F2/S1/B1/INV/1   |         |       |             |                      |        | last exception: List system inverter data failed because an invalid HTTP       |
	|                 |                   |           |                     |         |       |             |                      |        | status was returned: 429 TOO_MANY_REQUESTS                                     |
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	|            1001 | Enphase           |       234 | /S1F2/S1/B2/GEN/100 |     222 | c     |          51 | 2026-06-21T16:00:00Z | P7D    | Error executing rake task. Giving up [List system inverter data] after 1 try;  |
	|                 |                   |           | /S1F2/S1/B2/INV/1   |         |       |             |                      |        | last exception: List system inverter data failed because an invalid HTTP       |
	|                 |                   |           |                     |         |       |             |                      |        | status was returned: 429 TOO_MANY_REQUESTS                                     |
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	|            1002 | Enphase           |       345 | /S1F2/S1/B3/GEN/100 |     333 | q     |          23 | 2026-06-21T04:00:00Z | P7D    | Error executing rake task. Giving up [List system inverter data] after 1 try;  |
	|                 |                   |           | /S1F2/S1/B3/INV/1   |         |       |             |                      |        | last exception: List system inverter data failed because an invalid HTTP       |
	|                 |                   |           |                     |         |       |             |                      |        | status was returned: 429 TOO_MANY_REQUESTS                                     |
	+-----------------+-------------------+-----------+---------------------+---------+-------+-------------+----------------------+--------+--------------------------------------------------------------------------------+
	```

=== "CSV Output"

	```csv
	Datum Stream Count,Warning Count,Poll Task Count,Poll Warning Count,Rake Task Count,Rake Warning Count
	504,12,504,6,173,6

	Datum Streams Missing Rake Task:
	Datum Stream ID,Datum Stream Type,Object ID,Source ID,Enabled,Kind,Schedule,Mapping ID
	1004,eGauge,456,"/S1F2/S1/B4/GEN/100
	/S1F2/S1/B4/INV/1",true,n,0 0/30 * * * *,500

	Poll Tasks Stopped:
	Datum Stream ID,Datum Stream Type,Object ID,Source ID,State,Error Count,State,Start At,Message
	1000,Enphase,123,"/S1F2/S1/B1/GEN/100
	/S1F2/S1/B1/INV/1",c,7200,2026-06-28T17:50:21.376540Z,2026-06-18T00:15:00Z,"Error executing poll task. 429 Too Many Requests on GET request for ""https://api.enphaseenergy.com/api/v4/systems/4000000/telemetry/production_micro"": ""{""message"":""Too Many Requests"",""details"":""Usage limit exceeded for plan Kilowatt"",""code"":429}"""
	1001,Enphase,234,"/0257/S1/G1/GEN/100
	/0257/S1/G1/INV/100",c,7200,2026-06-28T19:28:49.383846Z,2026-06-18T01:00:00Z,"Error executing poll task. 429 Too Many Requests on GET request for ""https://api.enphaseenergy.com/api/v4/systems/4000001/telemetry/production_micro"": ""{""message"":""Too Many Requests"",""details"":""Usage limit exceeded for plan Kilowatt"",""code"":429}"""

	Poll Tasks Failing:
	Datum Stream ID,Datum Stream Type,Object ID,Source ID,State,Error Count,State,Start At,Message
	1003,Enphase,345,"/S1F2/S1/B3/GEN/100
	/S1F2/S1/B3/INV/1",c,7132,2026-06-28T17:50:21.376540Z,2026-06-18T00:15:00Z,"Error executing poll task. 429 Too Many Requests on GET request for ""https://api.enphaseenergy.com/api/v4/systems/4000002/telemetry/production_micro"": ""{""message"":""Too Many Requests"",""details"":""Usage limit exceeded for plan Kilowatt"",""code"":429}"""

	Poll Tasks Lagging:
	Datum Stream ID,Datum Stream Type,Object ID,Source ID,State,Error Count,State,Start At,Message
	1000,Enphase,123,"/S1F2/S1/B1/GEN/100
	/S1F2/S1/B1/INV/1",c,7200,2026-06-28T17:50:21.376540Z,2026-06-18T00:15:00Z,"Error executing poll task. 429 Too Many Requests on GET request for ""https://api.enphaseenergy.com/api/v4/systems/4000000/telemetry/production_micro"": ""{""message"":""Too Many Requests"",""details"":""Usage limit exceeded for plan Kilowatt"",""code"":429}"""
	1001,Enphase,234,"/0257/S1/G1/GEN/100
	/0257/S1/G1/INV/1",c,7200,2026-06-28T19:28:49.383846Z,2026-06-18T01:00:00Z,"Error executing poll task. 429 Too Many Requests on GET request for ""https://api.enphaseenergy.com/api/v4/systems/4000001/telemetry/production_micro"": ""{""message"":""Too Many Requests"",""details"":""Usage limit exceeded for plan Kilowatt"",""code"":429}"""
	1003,Enphase,345,"/S1F2/S1/B3/GEN/100
	/S1F2/S1/B3/INV/1",c,7132,2026-06-28T17:50:21.376540Z,2026-06-18T00:15:00Z,"Error executing poll task. 429 Too Many Requests on GET request for ""https://api.enphaseenergy.com/api/v4/systems/4000002/telemetry/production_micro"": ""{""message"":""Too Many Requests"",""details"":""Usage limit exceeded for plan Kilowatt"",""code"":429}"""

	Rake Tasks Stopped:
	Datum Stream ID,Datum Stream Type,Object ID,Source ID,Task ID,State,Error Count,Execute At,Offset,Message
	1000,Enphase,123,"/S1F2/S1/B1/GEN/100
	/S1F2/S1/B1/INV/1",111,c,51,2026-06-21T16:00:00Z,P7D,Error executing rake task. Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS
	1001,Enphase,234,"/S1F2/S1/B2/GEN/100
	/S1F2/S1/B2/INV/1",222,c,51,2026-06-21T16:00:00Z,P7D,Error executing rake task. Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS

	Rake Tasks Failing:
	Datum Stream ID,Datum Stream Type,Object ID,Source ID,Task ID,State,Error Count,Execute At,Offset,Message
	1003,Enphase,345,"/S1F2/S1/B3/GEN/100
	/S1F2/S1/B3/INV/1",333,c,23,2026-06-21T16:00:00Z,P7D,Error executing rake task. Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS

	Rake Tasks Lagging:
	Datum Stream ID,Datum Stream Type,Object ID,Source ID,Task ID,State,Error Count,Execute At,Offset,Message
	1000,Enphase,123,"/S1F2/S1/B1/GEN/100
	/S1F2/S1/B1/INV/1",111,c,51,2026-06-21T16:00:00Z,P7D,Error executing rake task. Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS
	1001,Enphase,234,"/S1F2/S1/B2/GEN/100
	/S1F2/S1/B2/INV/1",222,c,51,2026-06-21T16:00:00Z,P7D,Error executing rake task. Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS
	1003,Enphase,345,"/S1F2/S1/B3/GEN/100
	/S1F2/S1/B3/INV/1",333,c,23,2026-06-21T16:00:00Z,P7D,Error executing rake task. Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS
	```

=== "JSON Output"

	```json
	{
		"datumStreamCount": 504,
		"warningCount": 13,
		"datumStreamsWithWarnings": {
			"1000": {
				"configId": 1000,
				"name": "S1-R1",
				"serviceIdentifier": "s10k.c2c.ds.enphase",
				"created": "2025-06-06 02:33:16.601642Z",
				"modified": "2025-06-06 02:33:16.601642Z",
				"enabled": true,
				"datumStreamMappingId": 400,
				"schedule": "0 0/30 * * * *",
				"kind": "n",
				"objectId": 123,
				"sourceId": "unused",
				"serviceProperties": {
					"sourceIdMap": {
						"/4000000/inv/sys": "/S1F2/S1/B1/INV/1",
						"/4000000/met/sys": "/S1F2/S1/B1/GEN/100"
					}
				}
			},
			"1001": {
				"configId": 1001,
				"name": "S1-R2",
				"serviceIdentifier": "s10k.c2c.ds.enphase",
				"created": "2025-06-06 02:34:31.430303Z",
				"modified": "2025-06-06 02:34:31.430303Z",
				"enabled": true,
				"datumStreamMappingId": 400,
				"schedule": "0 5/30 * * * *",
				"kind": "n",
				"objectId": 234,
				"sourceId": "unused",
				"serviceProperties": {
					"sourceIdMap": {
						"/4000001/inv/sys": "/S1F2/S1/B2/INV/1",
						"/4000001/met/sys": "/S1F2/S1/B2/GEN/100"
					}
				}
			},
			"1002": {
				"configId": 1002,
				"name": "S1-R3",
				"serviceIdentifier": "s10k.c2c.ds.enphase",
				"created": "2025-06-06 02:36:18.102657Z",
				"modified": "2025-06-06 02:36:18.102657Z",
				"enabled": true,
				"datumStreamMappingId": 400,
				"schedule": "0 5/30 * * * *",
				"kind": "n",
				"objectId": 345,
				"sourceId": "unused",
				"serviceProperties": {
					"sourceIdMap": {
						"/4000002/inv/sys": "/S1F2/S1/B3/INV/1",
						"/4000002/met/sys": "/S1F2/S1/B3/GEN/100"
					}
				}
			},
			"1003": {
				"configId": 1003,
				"name": "S1-R4",
				"serviceIdentifier": "s10k.c2c.ds.egauge",
				"created": "2025-05-14 14:03:29.58754Z",
				"modified": "2025-05-14 14:32:23.129981Z",
				"enabled": true,
				"datumStreamMappingId": 500,
				"schedule": "0 0/30 * * * *",
				"kind": "n",
				"objectId": 456,
				"sourceId": "/S1F2/S1/B4/GEN/100",
				"serviceProperties": {
					"deviceId": "egauge00000",
					"password": "{SSHA-256}abcdef0123456789RyU1imCzjJjnJ2r4zu8v0mfgYOZG6vN0Q==",
					"username": "owner",
					"virtualSourceIds": [
						"/S1F2/S1/B4INV/1"
					]
				}
			}
		},
		"pollTasks": {
			"taskCount": 504,
			"warningCount": 6,
			"stoppedTasks": {
				"1000": {
					"datumStreamId": 1000,
					"state": "c",
					"executeAt": "2026-06-28 17:50:21.37654Z",
					"startAt": "2026-06-18 00:15:00Z",
					"message": "Error executing poll task.",
					"serviceProperties": {
						"message": "429 Too Many Requests on GET request for \"https://api.enphaseenergy.com/api/v4/systems/4000000/telemetry/production_micro\": \"{\"message\":\"Too Many Requests\",\"details\":\"Usage limit exceeded for plan Kilowatt\",\"code\":429}\"",
						"errorCount": 7200
					}
				},
				"1001": {
					"datumStreamId": 1001,
					"state": "c",
					"executeAt": "2026-06-28 19:28:49.383846Z",
					"startAt": "2026-06-18 01:00:00Z",
					"message": "Error executing poll task.",
					"serviceProperties": {
						"message": "429 Too Many Requests on GET request for \"https://api.enphaseenergy.com/api/v4/systems/4000001/telemetry/production_micro\": \"{\"message\":\"Too Many Requests\",\"details\":\"Usage limit exceeded for plan Kilowatt\",\"code\":429}\"",
						"errorCount": 7200
					}
				}
			},
			"errorTasks": {
				"1002": {
					"datumStreamId": 1002,
					"state": "q",
					"executeAt": "2026-06-28 19:47:51.642393Z",
					"startAt": "2026-06-18 02:45:00Z",
					"message": "Error executing poll task.",
					"serviceProperties": {
						"message": "429 Too Many Requests on GET request for \"https://api.enphaseenergy.com/api/v4/systems/4000002/telemetry/production_micro\": \"{\"message\":\"Too Many Requests\",\"details\":\"Usage limit exceeded for plan Kilowatt\",\"code\":429}\"",
						"errorCount": 7143
					}
				}
			},
			"laggingTasks": {
				"1000": {
					"datumStreamId": 1000,
					"state": "c",
					"executeAt": "2026-06-28 17:50:21.37654Z",
					"startAt": "2026-06-18 00:15:00Z",
					"message": "Error executing poll task.",
					"serviceProperties": {
						"message": "429 Too Many Requests on GET request for \"https://api.enphaseenergy.com/api/v4/systems/4000000/telemetry/production_micro\": \"{\"message\":\"Too Many Requests\",\"details\":\"Usage limit exceeded for plan Kilowatt\",\"code\":429}\"",
						"errorCount": 7200
					}
				},
				"1001": {
					"datumStreamId": 1001,
					"state": "c",
					"executeAt": "2026-06-28 19:28:49.383846Z",
					"startAt": "2026-06-18 01:00:00Z",
					"message": "Error executing poll task.",
					"serviceProperties": {
						"message": "429 Too Many Requests on GET request for \"https://api.enphaseenergy.com/api/v4/systems/4000001/telemetry/production_micro\": \"{\"message\":\"Too Many Requests\",\"details\":\"Usage limit exceeded for plan Kilowatt\",\"code\":429}\"",
						"errorCount": 7200
					}
				},
				"1002": {
					"datumStreamId": 1002,
					"state": "q",
					"executeAt": "2026-06-28 19:47:51.642393Z",
					"startAt": "2026-06-18 02:45:00Z",
					"message": "Error executing poll task.",
					"serviceProperties": {
						"message": "429 Too Many Requests on GET request for \"https://api.enphaseenergy.com/api/v4/systems/4000002/telemetry/production_micro\": \"{\"message\":\"Too Many Requests\",\"details\":\"Usage limit exceeded for plan Kilowatt\",\"code\":429}\"",
						"errorCount": 7143
					}
				}
			}
		},
		"rakeTasks": {
			"taskCount": 173,
			"warningCount": 7,
			"datumStreamsWithoutTasks": {
				"1003": {
					"configId": 1003,
					"name": "S1-R4",
					"serviceIdentifier": "s10k.c2c.ds.egauge",
					"created": "2025-05-14 14:03:29.58754Z",
					"modified": "2025-05-14 14:32:23.129981Z",
					"enabled": true,
					"datumStreamMappingId": 500,
					"schedule": "0 0/30 * * * *",
					"kind": "n",
					"objectId": 456,
					"sourceId": "/S1F2/S1/B4/GEN/100",
					"serviceProperties": {
						"deviceId": "egauge00000",
						"password": "{SSHA-256}abcdef0123456789RyU1imCzjJjnJ2r4zu8v0mfgYOZG6vN0Q==",
						"username": "owner",
						"virtualSourceIds": [
							"/S1F2/S1/B4INV/1"
						]
					}
				}
			},
			"stoppedTasks": {
				"1000": {
					"P7D": {
						"configId": 111,
						"datumStreamId": 1000,
						"state": "c",
						"executeAt": "2026-06-21 16:00:00Z",
						"offset": "P7D",
						"message": "Error executing rake task.",
						"serviceProperties": {
							"subId": 111,
							"message": "Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS",
							"configId": 1000,
							"errorCount": 51
						}
					}
				},
				"1001": {
					"P7D": {
						"configId": 222,
						"datumStreamId": 1002,
						"state": "c",
						"executeAt": "2026-06-21 16:00:00Z",
						"offset": "P7D",
						"message": "Error executing rake task.",
						"serviceProperties": {
							"subId": 222,
							"message": "Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS",
							"configId": 1001,
							"errorCount": 51
						}
					}
				}
			},
			"errorTasks": {
				"1002": {
					"P3D": {
						"configId": 333,
						"datumStreamId": 1002,
						"state": "q",
						"executeAt": "2026-06-29 04:00:00Z",
						"offset": "P3D",
						"message": "Rake task date is after poll task start.",
						"serviceProperties": {
							"subId": 333,
							"endDate": "2026-06-26 04:00:00Z",
							"message": "Giving up [List system inverter data] after 4 tries; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS",
							"startDate": "2026-06-18 00:15:00Z",
							"errorCount": 50
						}
					}
				}
			},
			"laggingTasks": {
				"1000": {
					"P7D": {
						"configId": 111,
						"datumStreamId": 1000,
						"state": "c",
						"executeAt": "2026-06-21 16:00:00Z",
						"offset": "P7D",
						"message": "Error executing rake task.",
						"serviceProperties": {
							"subId": 111,
							"message": "Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS",
							"configId": 1000,
							"errorCount": 51
						}
					}
				},
				"1001": {
					"P7D": {
						"configId": 222,
						"datumStreamId": 1002,
						"state": "c",
						"executeAt": "2026-06-21 16:00:00Z",
						"offset": "P7D",
						"message": "Error executing rake task.",
						"serviceProperties": {
							"subId": 222,
							"message": "Giving up [List system inverter data] after 1 try; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS",
							"configId": 1001,
							"errorCount": 51
						}
					}
				},
				"1002": {
					"P3D": {
						"configId": 333,
						"datumStreamId": 1002,
						"state": "q",
						"executeAt": "2026-06-29 04:00:00Z",
						"offset": "P3D",
						"message": "Rake task date is after poll task start.",
						"serviceProperties": {
							"subId": 333,
							"endDate": "2026-06-26 04:00:00Z",
							"message": "Giving up [List system inverter data] after 4 tries; last exception: List system inverter data failed because an invalid HTTP status was returned: 429 TOO_MANY_REQUESTS",
							"startDate": "2026-06-18 00:15:00Z",
							"errorCount": 50
						}
					}
				}
			}
		}
	}
	```


[datum-stream]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream
