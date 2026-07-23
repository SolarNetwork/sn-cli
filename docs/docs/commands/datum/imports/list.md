---
title: list
---
# Datum Imports List

Show [datum import job status][datum-import-status] matching a search filter.

## Usage

```
s10k datum imports list
	[-state=jobState[,jobState...]]...
	[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-state=` | `--job-state=` | the job state(s) to match, one of `Staged`, `Retracted`, `Queued`, `Claimed`, `Executing`, or `Completed`; see the [wiki][import-state] for more info |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of all matching job states.

## Examples

View staged imports (yet to be actioned):

=== "List staged import jobs"

	```sh
	s10k datum imports list --job-state Staged
	```

=== "Pretty Output"

	```
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	| Job Name                                 | Job ID                               | Group ID                             | Submit Date              | State  | Import Date              | Success | Started At | Completed At | Loaded | % Complete | Batch Size | Input Service | Input Time Zone | Input Properties                             |
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	| 1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import | 49a2f730-0000-0000-0000-233d085c799a | 0300b452-f957-4dd2-b8fd-43e7c715601d | 2026-07-22T00:37:12.548Z | Staged | 2026-07-22T00:37:09.862Z |         |            |              |      0 | 0.0        |            | CSV - Simple  | UTC             | dateFormat               yyyy-MM-dd HH:mm:ss |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | nodeIdColumn             1                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | headerRowCount           1                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | sourceIdColumn           2                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | dateColumnsValue         3                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | accumulatingDataColumns  5                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | instantaneousDataColumns 4                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 |                                              |
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	```

=== "CSV Output"

	```csv
	Job Name,Job ID,Group ID,Submit Date,State,Import Date,Success,Started At,Completed At,Loaded,% Complete,Batch Size,Input Service,Input Time Zone,Input Properties
	1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import,49a2f730-0000-0000-0000-233d085c799a,0300b452-f957-4dd2-b8fd-43e7c715601d,2026-07-22T00:37:12.548Z,Staged,2026-07-22T00:37:09.862Z,,,,0,0.0,,CSV - Simple,UTC,"dateFormat               yyyy-MM-dd HH:mm:ss
	nodeIdColumn             1
	headerRowCount           1
	sourceIdColumn           2
	dateColumnsValue         3
	accumulatingDataColumns  5
	instantaneousDataColumns 4
	"
	```

=== "JSON Output"

	```json
	[
		{
			"userId" : 857,
			"jobId" : "49a2f730-0000-0000-0000-233d085c799a",
			"jobState" : "Staged",
			"importDate" : "2026-07-22 00:37:09.862Z",
			"groupKey" : "0300b452-f957-4dd2-b8fd-43e7c715601d",
			"success" : false,
			"submitDate" : "2026-07-22 00:37:12.548Z",
			"loadedCount" : 0,
			"percentComplete" : 0.0,
			"configuration" : {
				"name" : "1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import",
				"stage" : true,
				"inputConfiguration" : {
					"name" : "1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Input",
					"serviceIdentifier" : "net.solarnetwork.central.datum.imp.standard.SimpleCsvDatumImportInputFormatService",
					"serviceProperties" : {
						"dateFormat" : "yyyy-MM-dd HH:mm:ss",
						"nodeIdColumn" : "1",
						"headerRowCount" : "1",
						"sourceIdColumn" : "2",
						"dateColumnsValue" : "3",
						"accumulatingDataColumns" : "5",
						"instantaneousDataColumns" : "4"
					},
					"timeZoneId" : "UTC"
				}
			}
		}
	]
	```

View completed imports:

=== "List completed imports"

	```sh
	s10k datum imports list --job-state Completed
	```

=== "Pretty Output"

	```
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+-----------+--------------------------+---------+--------------------------+--------------------------+--------+---------------------+------------+-------------------+------------------+----------------------------------------------+
	| Job Name                                 | Job ID                               | Group ID                             | Submit Date              | State     | Import Date              | Success | Started At               | Completed At             | Loaded | % Complete          | Batch Size | Input Service     | Input Time Zone  | Input Properties                             |
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+-----------+--------------------------+---------+--------------------------+--------------------------+--------+---------------------+------------+-------------------+------------------+----------------------------------------------+
	| Stream-Import-2026-07-22T20:50:08.122Z   | c812b552-0000-0000-0000-7ab31a35c6a0 | stream-1000                          | 2026-07-22T20:50:08.815Z | Completed | 2026-07-22T20:50:08.816Z | true    | 2026-07-22T20:50:12.199Z | 2026-07-22T20:50:25.463Z |   6286 | 1.0                 | 1          | Cloud Integration | Pacific/Auckland | endDate       2026-07-22T20:47:34.143Z       |
	|                                          |                                      |                                      |                          |           |                          |         |                          |                          |        |                     |            |                   |                  | startDate     2026-07-01T01:00:00.000Z       |
	|                                          |                                      |                                      |                          |           |                          |         |                          |                          |        |                     |            |                   |                  | datumStreamId 1000                           |
	|                                          |                                      |                                      |                          |           |                          |         |                          |                          |        |                     |            |                   |                  |                                              |
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+-----------+--------------------------+---------+--------------------------+--------------------------+--------+---------------------+------------+-------------------+------------------+----------------------------------------------+
	```

=== "CSV Output"

	```csv
	Job Name,Job ID,Group ID,Submit Date,State,Import Date,Success,Started At,Completed At,Loaded,% Complete,Batch Size,Input Service,Input Time Zone,Input Properties
	Stream-Import-2026-07-22T20:50:08.122Z,c812b552-0000-0000-0000-7ab31a35c6a0,stream-1045,2026-07-22T20:50:08.815Z,Completed,2026-07-22T20:50:08.816Z,true,2026-07-22T20:50:12.199Z,2026-07-22T20:50:25.463Z,6286,1.0,1,Cloud Integration,Pacific/Auckland,"endDate       2026-07-22T20:47:34.143Z
	startDate     2026-07-01T01:00:00.000Z
	datumStreamId 1045
	"
	```

=== "JSON Output"

	```json
	[
		{
			"userId": 857,
			"jobId": "c812b552-0000-0000-0000-7ab31a35c6a0",
			"jobState": "Completed",
			"importDate": "2026-07-22 20:50:08.816Z",
			"groupKey": "stream-1000",
			"success": true,
			"submitDate": "2026-07-22 20:50:08.815Z",
			"startedDate": "2026-07-22 20:50:12.199Z",
			"completionDate": "2026-07-22 20:50:25.463Z",
			"loadedCount": 6286,
			"percentComplete": 1.0,
			"configuration": {
				"name": "Stream-Import-2026-07-22T20:50:08.122Z",
				"stage": false,
				"batchSize": 1,
				"inputConfiguration": {
					"name": "Cloud Datum Stream Import",
					"serviceIdentifier": "s10k.c2c.ds-import",
					"serviceProperties": {
						"endDate": "2026-07-22T20:47:34.143Z",
						"startDate": "2026-07-01T01:00:00.000Z",
						"datumStreamId": 1000
					},
					"timeZoneId": "Pacific/Auckland"
				}
			}
		}
	]
	```

[datum-import-status]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Datum-Import-API#import-task-view-response
[import-state]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#datum-import-task-state-type
