---
title: view
---
# Datum Imports View

View status information about a datum import job.

## Usage

```
s10k datum imports view
	-j=<jobId>
	[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-j=` | `--job-id=` | the ID of the job to view |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

The updated job info.

# Examples

=== "View datum import"

	```sh
	s10k datum imports view --job-id 49a2f730-0000-0000-0000-233d085c799a
	```

=== "View datum import (shortcut)"

	You can use `imp` instead of `imports`:

	```sh
	s10k datum imp view --job-id 49a2f730-0000-0000-0000-233d085c799a
	```

=== "Pretty Output"

	```
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	| Job Name                                 | Job ID                               | Group ID                             | Submit Date              | State  | Import Date              | Success | Started At | Completed At | Loaded | % Complete | Batch Size | Input Service | Input Time Zone | Input Properties                             |
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	| 1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import | 49a2f730-0000-0000-0000-233d085c799a | 0300b452-0000-0000-0000-43e7c715601d | 2026-07-22T00:37:12.548Z | Staged | 2026-07-22T00:37:09.862Z |         |            |              |      0 |          0 |            | CSV - Simple  | UTC             | dateFormat               yyyy-MM-dd HH:mm:ss |
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
	1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import,49a2f730-0000-0000-0000-233d085c799a,0300b452-0000-0000-0000-43e7c715601d,2026-07-22T00:37:12.548Z,Staged,2026-07-22T00:37:09.862Z,,,,0,0,,CSV - Simple,UTC,"dateFormat               yyyy-MM-dd HH:mm:ss
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
	    "jobState": "Staged",
	    "importDate": "2026-07-22 00:37:09.862Z",
	    "groupKey": "0300b452-0000-0000-0000-43e7c715601d",
	    "success": false,
	    "submitDate": "2026-07-22 00:37:12.548Z",
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
