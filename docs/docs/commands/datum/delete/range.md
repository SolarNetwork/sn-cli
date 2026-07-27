---
title: range
---
# Datum Delete Range

Delete datum within a time range.

## Usage

```
s10k datum delete range
	[-node=nodeId[,nodeId...]]...
	[-source=sourceId[,sourceId...]]...
	-min=<minDate>
	-max=<maxDate>
    [-agg=<aggregation>]
	[-mode=<displayMode>]
```

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to preview the operation,
	without actually changing anything. For example:

	```sh
	s10k --dry-run datum delete range --node-id 123 --min-date 2026-01-01 --max-date 2027-01-01
	```


## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID(s) of datum to delete (or all node IDs if not provided) |
| `-source=` | `--source=` | the source ID(s) of datum to delete (or all soruce IDs if not provided) |
| `-min=` | `--min-date=` | a minimum date to limit results to, like `2020-10-30` or `2020-10-30T12:45`; will be treated as a node-local time zone |
| `-max=` | `--max-date=` | a maximum date (exclusive) to limit results to, in same form as `-min`; will be treated as a node-local time zone |
| `-agg=` | `--aggregation=` | a maximum [aggregation type][aggregation] to delete (inclusive), or all aggregations if not provided; **note** this option is ignored if `--dry-run` given |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

When the `--dry-run` option is given, information will be returned that details an estimate of how
many datum would be deleted based on the given options.

| Property | Description |
|:---------|:------------|
| Date | The date the result was calculated (will be close to the current time). |
| Total Count | The total number of datum, and aggregate datum, that would be deleted. |
| Raw Count | The number of raw (non-aggregate) datum that would be deleted. |
| Hourly Count | The number of hour-level aggregate datum that would be deleted. |
| Daily Count | The number of day-level aggregate datum that would be deleted. |
| Monthly Count | The number of month-level aggregate datum that would be deleted. |

Without the `--dry-run` option, a delete job will be submitted and its status returned,
which will include the job ID you can use to montior the delete task's progress.

## Examples

Use the `--dry-run` global option to get a count of datum that would be deleted:

=== "Preview delete"

	Here we will update the job name and time zone, as well as change the `dateFormat`
	service property:

	```sh
	s10k --dry-run datum delete range --node-id 100 --min-date 2026-01-01 --max-date 2027-01-01
	```

=== "Preview datum delete (shortcut)"

	You can use `del` instead of `delete`:

	```sh
	s10k --dry-run datum del range --node-id 100 --min-date 2026-01-01 --max-date 2027-01-01
	```

=== "Pretty Output"

	```
	+---------------------------------+--------+-----------+--------------+-------------+---------------+
	| Date                            | Total  | Raw Datum | Hourly Datum | Daily Datum | Monthly Datum |
	+---------------------------------+--------+-----------+--------------+-------------+---------------+
	| 2026-07-28 10:28:38.72032+12:00 | 227405 |    223206 |         3940 |         227 |            32 |
	+---------------------------------+--------+-----------+--------------+-------------+---------------+
	```

=== "CSV Output"

	```csv
	Date,Total,Raw Datum,Hourly Datum,Daily Datum,Monthly Datum
	2026-07-28 10:37:16.411877+12:00,227405,223206,3940,227,32
	```

=== "JSON Output"

	```json
	{
	  "date" : "2026-07-27 22:37:30.757556Z",
	  "datumTotalCount" : 227405,
	  "datumCount" : 223206,
	  "datumHourlyCount" : 3940,
	  "datumDailyCount" : 227,
	  "datumMonthlyCount" : 32
	}
	```

Omit the `--dry-run` option to submit the delete task, and get the task information as output:

=== "Delete datum"

	Here we will update the job name and time zone, as well as change the `dateFormat`
	service property:

	```sh
	s10k datum delete range --node-id 100 --min-date 2026-01-01 --max-date 2027-01-01
	```

=== "Delete datum (shortcut)"

	You can use `del` instead of `delete`:

	```sh
	s10k datum del range --node-id 100 --min-date 2026-01-01 --max-date 2027-01-01
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


[job-state]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#claimable-job-state-type
