---
title: mark
---
# Datum Stale Aggregates Mark

Mark node datum stream aggregations as "stale" for ranges of time.

## Usage

```
s10k datum stale-aggregates mark
	[-node=nodeId[,nodeId...]]...
	[-source=sourceId[,sourceId...]]...
	[-ident=identifier[,identifier...]]...
	[-min=<minDate>] [-max=<maxDate>] [-local] [-tz=<zone>]
    [-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-ident=` | `--stream-ident=` | an `object:source` stream identifier to show records for; if provided then `-node` and `-source` are ignored |
| `-local` | `--local-dates` | treat the min/max dates as "node local" dates, instead of UTC (or the local time zone when `-tz` used) |
| `-max=` | `--max-date=` | a maximum date (exclusive) to limit results to like `2020-10-30` or `2020-10-30T12:45` |
| `-min=` | `--min-date=` | a minimum date to limit results to, in same form as `-max` |
| `-node=` | `--node-id=` | the node ID(s) to show records for |
| `-source=` | `--source=` | the source ID(s) to show records for |
| `-tz=` | `--time-zone=` | a time zone ID to treat the min/max dates as instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of stale aggregate records matching the criteria. Note that if the `--local-dates` option is used
then the results will include records matching the given node and source IDs but for all time.

## Examples

=== "Mark aggregates stale"

	```sh
	s10k datum stale-aggregates mark --node-id 123 --source-id 'bld1/meter/*'  --min-date 2026-04-15 --max-date 2026-04-16
	```

=== "Mark aggregates stale (shortcut)"

	You can use `stale-agg` or `stale` instead of `stale-aggregates`:

	```sh
	s10k datum stale mark --node-id 123 --source-id 'bld1/meter/*'  --min-date 2026-04-15 --max-date 2026-04-16
	```

=== "Pretty Output"

	```
	+------+---------+--------------+---------------------------+
	| Kind | Node ID | Source ID    | Period Start              |
	+------+---------+--------------+---------------------------+
	| Hour |     123 | bld1/meter/4 | 2026-04-15 10:00:00+12:00 |
	+------+---------+--------------+---------------------------+
	| Hour |     123 | bld1/meter/4 | 2026-04-15 11:00:00+12:00 |
	+------+---------+--------------+---------------------------+
	| Hour |     123 | bld1/meter/4 | 2026-04-15 12:00:00+12:00 |
	+------+---------+--------------+---------------------------+
	| Hour |     123 | bld1/meter/4 | 2026-04-15 13:00:00+12:00 |
	+------+---------+--------------+---------------------------+
	```

=== "CSV Output"

	```csv
	Kind,Node ID,Source ID,Period Start
	Hour,123,bld1/meter/4,2026-04-15 10:00:00+12:00
	Hour,123,bld1/meter/4,2026-04-15 11:00:00+12:00
	Hour,123,bld1/meter/4,2026-04-15 12:00:00+12:00
	Hour,123,bld1/meter/4,2026-04-15 13:00:00+12:00
	```

=== "JSON Output"

	```json
	[
	  {
	    "kind": "Hour",
	    "nodeId": 123,
	    "sourceId": "bld1/meter/4",
	    "startDate": "2026-04-14 22:00:00Z"
	  },
	  {
	    "kind": "Hour",
	    "nodeId": 123,
	    "sourceId": "bld1/meter/4",
	    "startDate": "2026-04-14 23:00:00Z"
	  },
	  {
	    "kind": "Hour",
	    "nodeId": 123,
	    "sourceId": "bld1/meter/4",
	    "startDate": "2026-04-15 00:00:00Z"
	  },
	  {
	    "kind": "Hour",
	    "nodeId": 123,
	    "sourceId": "bld1/meter/4",
	    "startDate": "2026-04-15 01:00:00Z"
	  }
	]
	```

[rake-task]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-rake-task
