---
title: confirm-staged
---
# Datum Imports Confirm Staged

Confirm a staged datum import job, so it may begin the import process.

!!! note

	This command will only work for jobs in the `Staged` [state][import-state].

## Usage

```
s10k datum imports confirm-staged
	-j=<jobId>
	[-mode=<displayMode>]
```

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to preview the update,
	without actually changing anything. For example:

	```sh
	s10k --dry-run datum imports confirm-staged --job-id 49a2f730-0000-0000-0000-233d085c799a
	```


## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-j=` | `--job-id=` | the ID of the staged job to confirm |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

The updated job info.

# Examples

=== "Preview staged datum import"

	```sh
	s10k datum imports confirm-staged --job-id 49a2f730-0000-0000-0000-233d085c799a
	```

=== "Preview staged datum import (shortcut)"

	You can use `imp` instead of `imports` and `confirm` instead of `confirm-staged`:

	```sh
	s10k datum imp confirm --job-id 49a2f730-0000-0000-0000-233d085c799a
	```

=== "Pretty Output"

	```
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	| Job Name                                 | Job ID                               | Group ID                             | Submit Date              | State  | Import Date              | Success | Started At | Completed At | Loaded | % Complete | Batch Size | Input Service | Input Time Zone | Input Properties                             |
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	| 1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import | 49a2f730-0000-0000-0000-233d085c799a | 0300b452-0000-0000-0000-43e7c715601d | 2026-07-22T00:37:12.548Z | Queued | 2026-07-22T00:37:09.862Z |         |            |              |      0 | 0.0        |            | CSV - Simple  | UTC             | dateFormat               yyyy-MM-dd HH:mm:ss |
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
	1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import,49a2f730-0000-0000-0000-233d085c799a,0300b452-0000-0000-0000-43e7c715601d,2026-07-22T00:37:12.548Z,Queued,2026-07-22T00:37:09.862Z,,,,0,0.0,,CSV - Simple,UTC,"dateFormat               yyyy-MM-dd HH:mm:ss
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
	    "userId": 857,
	    "jobId": "49a2f730-0000-0000-0000-233d085c799a",
	    "jobState": "Queued",
	    "importDate": "2026-07-22 00:37:09.862Z",
	    "groupKey": "0300b452-0000-0000-0000-43e7c715601d",
	    "success": false,
	    "submitDate": "2026-07-22 00:37:12.548Z",
	    "startedDate": "1970-01-01 00:00:00Z",
	    "completionDate": "1970-01-01 00:00:00Z",
	    "loadedCount": 0,
	    "percentComplete": 0.0,
	    "configuration": {
	      "name": "1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import",
	      "stage": true,
	      "inputConfiguration": {
	        "name": "1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Input",
	        "serviceIdentifier": "net.solarnetwork.central.datum.imp.standard.SimpleCsvDatumImportInputFormatService",
	        "serviceProperties": {
	          "dateFormat": "yyyy-MM-dd HH:mm:ss",
	          "nodeIdColumn": "1",
	          "headerRowCount": "1",
	          "sourceIdColumn": "2",
	          "dateColumnsValue": "3",
	          "accumulatingDataColumns": "5",
	          "instantaneousDataColumns": "4"
	        },
	        "timeZoneId": "UTC"
	      }
	    }
	  }
	]
	```

[import-state]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#datum-import-task-state-type
