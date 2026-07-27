---
title: view
---
# Datum Delete View

View status information about a datum delete job.

## Usage

```
s10k datum delete view
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

The job info.

## Examples

=== "View datum delete"

	```sh
	s10k datum delete view --job-id 49a2f730-0000-0000-0000-233d085c799a
	```

=== "View datum delete (shortcut)"

	You can use `del` instead of `delete`:

	```sh
	s10k datum del view --job-id 49a2f730-0000-0000-0000-233d085c799a
	```

=== "Pretty Output"

	```
	+--------------------------------------+-------------------------------+--------+---------+------------+--------------+---------+------------+-------------------------------------+
	| Job ID                               | Submit Date                   | State  | Success | Started At | Completed At | Deleted | % Complete | Delete Criteria                     |
	+--------------------------------------+-------------------------------+--------+---------+------------+--------------+---------+------------+-------------------------------------+
	| 49a2f730-0000-0000-0000-233d085c799a | 2026-07-28 11:18:25.135+12:00 | Queued |         |            |              |       0 |          0 | nodeIds        2,3,4,5,10,13,14,123 |
	|                                      |                               |        |         |            |              |         |            | localStartDate 2025-01-01           |
	|                                      |                               |        |         |            |              |         |            | localEndDate   2025-01-02           |
	|                                      |                               |        |         |            |              |         |            | aggregation    None                 |
	|                                      |                               |        |         |            |              |         |            |                                     |
	+--------------------------------------+-------------------------------+--------+---------+------------+--------------+---------+------------+-------------------------------------+
	```

=== "CSV Output"

	```csv
	Job ID,Submit Date,State,Success,Started At,Completed At,Deleted,% Complete,Delete Criteria
	49a2f730-0000-0000-0000-233d085c799a,2026-07-28 11:19:32.32+12:00,Queued,,,,0,0,"nodeIds        2,3,4,5,10,13,14,123
	localStartDate 2025-01-01
	localEndDate   2025-01-02
	aggregation    None
	"
	```

=== "JSON Output"

	```json
	{
	  "userId": 147,
	  "jobId": "49a2f730-0000-0000-0000-233d085c799a",
	  "jobState": "q",
	  "success": false,
	  "submitDate": "2026-07-27 23:19:59.992Z",
	  "resultCount": 0,
	  "percentComplete": 0.0,
	  "jobDuration": 0,
	  "configuration": {
	    "objectKind": "n",
	    "localStartDate": "2025-01-01 00:00:00",
	    "localEndDate": "2025-01-02 00:00:00",
	    "mostRecent": false,
	    "withoutTotalResultsCount": false,
	    "aggregation": "None",
	    "partialAggregation": "None",
	    "readingRecordStyle": false,
	    "readingAggregateStyle": false,
	    "readingStyle": false,
	    "locationQuery": false,
	    "aggregateStyle": false,
	    "nodeIds": [
	      2,
	      3,
	      4,
	      5,
	      10,
	      13,
	      14,
	      123
	    ]
	  },
	  "deleting": true
	}
	```
