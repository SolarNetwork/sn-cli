---
title: date-range
---
# Datum Date Range

Discover the available time range of datum on a node.

## Usage

```
s10k datum date-range
	(-node=<nodeId> | -loc=<locationId>)
	[-source=<sourceId>]
	[-min=<minDate>]
    [-max=<maxDate>]
	[-local]
	[-tz=<zone>]
    [-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-loc=` | `--location-id=` | the location ID to show information for (exclusive to `-node`) |
| `-node=` | `--node-id=` | the node ID to show information for (exclusive to `-loc`) |
| `-source=` | `--source=` | a source IDs to restrict the date range information to |
| `-min=` | `--min-date=` | a minimum date to limit results to, like `2020-10-30` or `2020-10-30T12:45` |
| `-max=` | `--max-date=` | a maximum date (exclusive) to limit results to, in same form as `-min` |
| `-local` | `--local-dates` | treat the min/max dates as "node local" dates, instead of UTC (or the local time zone when `-tz` used) |
| `-tz=` | `--time-zone=` | a time zone ID to treat the min/max dates as instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

Information about the time span of available data matching the criteria. The returned information includes:

| Property | Description |
|:---------|:------------|
| **Start Date** | The earliest datum timestamp available matching the criteria, in `yyyy-MM-dd HH:mm:ss.S` format; the time zone will be the given **Time Zone* property value. |
| **End Date** | The latest datum timestamp available matching the criteria, in `yyyy-MM-dd HH:mm:ss.S` format; the time zone will be the given **Time Zone* property value.. |
| **Time Zone** | The time zone both **Start Date** and **End Date** are given in, for example `Pacific/Auckland`. |
| **Year Count** | The number of years represented by the interval between **Start Date** and **End Date**; partial years are rounded up. |
| **Month Count** | The number of months represented by the interval between **Start Date** and **End Date**; partial months are rounded up. |
| **Day Count** | The number of days represented by the interval between **Start Date** and **End Date**; partial days are rounded up. |

## Examples

Show the available date range for all sources on a node:

=== "View node date range"

	```sh
	s10k datum date-range --node-id 101
	```

=== "View node date range (shortcut)"

	You can use `range` instead of `date-range`:

	```sh
	s10k datum range --node-id 101
	```

=== "Pretty Output"

	```
	+---------------------+---------------------+------------------+-------+--------+------+
	| Start Date          | End Date            | Time Zone        | Years | Months | Days |
	+---------------------+---------------------+------------------+-------+--------+------+
	| 2014-01-01 00:00:00 | 2026-07-26 18:04:06 | America/New_York |    13 |    151 | 4590 |
	+---------------------+---------------------+------------------+-------+--------+------+
	```

=== "CSV Output"

	```csv
	Start Date,End Date,Time Zone,Years,Months,Days
	2014-01-01 00:00:00,2026-07-26 18:04:06,America/New_York,13,151,4590
	```

=== "JSON Output"

	```json
	{
	  "startDate" : "2014-01-01 00:00:00",
	  "endDate" : "2026-07-26 18:04:06",
	  "timeZone" : "America/New_York",
	  "yearCount" : 13,
	  "monthCount" : 151,
	  "dayCount" : 4590
	}
	```
