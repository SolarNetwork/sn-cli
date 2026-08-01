---
title: list
---
# Datum Stream Stale Aggregates List

Show [stale node datum aggregate records][stale-aggs-response] matching a search filter.

## Usage

```
s10k datum stale-aggregates list
	[-node=nodeId[,nodeId...]]...
	[-source=sourceId[,sourceId...]]...
	[-ident=identifier[,identifier...]]...
	[-min=<minDate>] [-max=<maxDate>] [-tz=<zone>]
	[-agg=<aggregation>]
	[-M=max] [-O=<resultOffset>]
	[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-agg=` | `--aggregation=` | the [aggregation type][aggregation] to return |
| `-ident=` | `--stream-ident=` | an `object:source` stream identifier to show records for; if provided then `-node` and `-source` are ignored |
| `-M=` | `--max=` | the maximum number of results to return |
| `-max=` | `--max-date=` | a maximum date (exclusive) to limit results to like `2020-10-30` or `2020-10-30T12:45` |
| `-min=` | `--min-date=` | a minimum date to limit results to, in same form as `-max` |
| `-node=` | `--node-id=` | the node ID(s) to show records for |
| `-O=` | `--offset=` | start returning results from this offset, `0` being the first result |
| `-source=` | `--source=` | the source ID(s) to show records for |
| `-tz=` | `--time-zone=` | a time zone ID to treat the min/max dates as instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of matching stale aggregate records. A stale aggregate record represents an aggregate
period of time on a datum stream that SolarNetwork needs to recalculate because the underlying data
has changed. Each record contains the following properties:

| Property | Description |
|:---------|:------------|
| Kind | The [aggregation type][aggregation]. |
| Node ID | The node ID. |
| Source ID | The soruce ID. |
| Period Start | The start of the aggreate period. The end of the period depends on the **Kind** value. |


## Examples

View all stale aggregate records for a node:

=== "Show stale records for node"

	```sh
	s10k datum stale-aggregates list --node-id 101
	```

=== "Show stale records for node (shortcut)"

	You can use `stale-agg` or `stale` instead of `stale-aggregates`:

	```sh
	s10k datum stale list --node-id 1001
	```

=== "Pretty Output"

	```
	+------+---------+---------------------+---------------------------+
	| Kind | Node ID | Source ID           | Period Start              |
	+------+---------+---------------------+---------------------------+
	| Day  |    1001 | /BLD1/S1/R10/INV/5  | 2026-07-31 00:00:00-07:00 |
	+------+---------+---------------------+---------------------------+
	| Hour |    1001 | /BLD1/S1/R10/INV/5  | 2026-07-31 21:00:00-07:00 |
	+------+---------+---------------------+---------------------------+
	```

=== "CSV Output"

	```csv
	Kind,Node ID,Source ID,Period Start
	Day,1001,/BLD1/S1/R10/INV/5,2026-07-31 00:00:00-07:00
	Hour,1001,/BLD1/S1/R10/INV/5,2026-07-31 21:00:00-07:00
	```

=== "JSON Output"

	```json
	[
	  {
	    "kind": "Day",
	    "nodeId": 1001,
	    "sourceId": "/BLD1/S1/R10/INV/5",
	    "startDate": "2026-07-31 07:00:00Z"
	  },
	  {
	    "kind": "Hour",
	    "nodeId": 1001,
	    "sourceId": "/BLD1/S1/R10/INV/5",
	    "startDate": "2026-08-01 04:00:00Z"
	  }
	]
	```

[aggregation]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarQuery-API-enumerated-types#aggregation-types
[stale-aggs-response]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API#datum-maintenance-aggregates-stale-list-response
