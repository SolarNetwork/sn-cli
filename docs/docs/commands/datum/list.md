---
title: list
---
# Datum List

List datum matching a search filter.

!!! info

	For more details on the SolarNetwork APIs used by this command, see the documentation for the
	[/datum/stream/datum][stream-list] and [/datum/stream/reading][stream-reading] endpoints.

# Usage

```
s10k datum list [-local] [-recent] [--with-total-result-count]
                [-agg=<aggregation>] [-max=<maxDate>] [-min=<minDate>]
                [-mode=<displayMode>] [-pagg=<partialAggregation>]
                [-read=<readingType>] [-tol=<timeTolerance>]
                [-tz=<zone>] [-prop=propName[,propName...]]...
                [-source=sourceId[,sourceId...]]...
				[-stream=streamId[,streamId...]]...
				[
					-node=nodeId[,nodeId...] [-node=nodeId[,nodeId...]]... |
					-loc=locId[,locId...] [-loc=locId[,locId...]]...
				]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the stream ID(s) to show |
| `-node=` | `--node-id=` | the node ID(s) to show metadata for |
| `-node=` | `--node-id=` | the node ID(s) to show stream metadata for (exclusive to `-loc`) |
| `-loc=` | `--location-id=` | the location ID(s) to show stream metadata for (exclusive to `-node`) |
| `-source=` | `--source=` | the source ID(s) to show stream metadata for |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY`; **note** that `PRETTY` is not suitable for large result sets |
| `-prop=` | `--property=` | restrict results to metadata that has this property (instantaneous, accumulating, **or** status); multiple properties combine with logical "or" |
| `-min=` | `--min-date=` | a minimum date to limit results to, like `2020-10-30` or `2020-10-30T12:45` |
| `-max=` | `--max-date=` | a maximum date (exclusive) to limit results to, in same form as `-min` |
| `-tz=` | `--time-zone=` | a time zone ID to treat the min/max dates as, instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-local` | `--local-dates` | treat the min/max dates as "node local" dates, instead of UTC (or the local time zone when `-tz` used) |
| `-recent` | `--most-recent` | show just the most recently available data, within min/max dates if specified |
| `-agg=` | `--aggregate=` | the [aggregation type][aggregation] to return |
| `-pagg=` | `--partial-aggregate=` | a [partial aggregation][partial-aggregation] level to use |
| `-read=` | `--reading=` | return [reading][reading] aggregation results instead of listing results |
| `-tol=` | `--tolerance` | a time tolerance to use with reading-style queries that support it, as an ISO period like `P7D` for 7 days |
| `-S` | `--show-stream-ids` | show stream IDs in `PRETTY` results |
| `-M=` | `--max=` | the maximum number of results to return |
| `-O=` | `--offset=` | start returning results from this offset, `0` being the first result |

</div>

## Output

A listing of all matching datum.

## Examples

List hour-level aggregate datum over a date range:

=== "List hourly datum"

	```sh
	s10k --profile demo datum list --node-id 101 --source-id con/1 \
	  --min-date 2025-08-21 --max-date 2025-08-22 --aggregate Hour
	```

=== "Pretty Output"

	```
	+----------------------+-----------+-----------+----------+---------+---------+-----------+----------+-----------+
	| Timestamp Start      | Object ID | Source ID | watts    | current | voltage | frequency | pcmLimit | wattHours |
	+----------------------+-----------+-----------+----------+---------+---------+-----------+----------+-----------+
	| 2025-08-20T21:00:00Z | 101       | con/1     | 2473.904 |  10.749 | 230.149 |    49.976 |          |    2078.5 |
	+----------------------+-----------+-----------+----------+---------+---------+-----------+----------+-----------+
	| 2025-08-20T22:00:00Z | 101       | con/1     | 2374.817 |  10.308 | 230.273 |    50.004 |          |    2347.3 |
	+----------------------+-----------+-----------+----------+---------+---------+-----------+----------+-----------+
	| 2025-08-20T23:00:00Z | 101       | con/1     | 2570.453 |  11.171 | 230.085 |    50.004 |          | -4254.242 |
	+----------------------+-----------+-----------+----------+---------+---------+-----------+----------+-----------+
	| 2025-08-21T00:00:00Z | 101       | con/1     |   1289.5 |   5.656 | 228.478 |    49.826 |          |     8.442 |
	+----------------------+-----------+-----------+----------+---------+---------+-----------+----------+-----------+
	```

=== "CSV Output"

	```csv
	ts_start,ts_end,streamId,objectId,sourceId,watts,watts_count,watts_min,watts_max,current,current_count,current_min,current_max,voltage,voltage_count,voltage_min,voltage_max,frequency,frequency_count,frequency_min,frequency_max,pcmLimit,pcmLimit_count,pcmLimit_min,pcmLimit_max,wattHours,wattHours_start,wattHours_end,tags
	2025-08-20T21:00:00Z,,03c6bd01-9241-4771-ad3c-a1d5eb06b68a,101,con/1,2473.9038461538461538,52,1153,3518,10.7490174230769231,52,5.0912933,14.998348,230.1490155769230769,52,225.00418,234.99796,49.9755515576923077,52,49.800114,50.199997,,0,,,2078.5,8186363,8225742,
	2025-08-20T22:00:00Z,,03c6bd01-9241-4771-ad3c-a1d5eb06b68a,101,con/1,2374.8166666666666667,60,1130,3501,10.3080711733333333,60,5.0031877,14.948236,230.273054,60,225.00723,234.97006,50.0040209666666667,60,49.800064,50.2,,0,,,2347.3,8225742,8228086,
	2025-08-20T23:00:00Z,,03c6bd01-9241-4771-ad3c-a1d5eb06b68a,101,con/1,2570.4528301886792453,53,1154,3520,11.1705456301886792,53,5.083911,14.998905,230.085467358490566,53,225.00075,234.99974,50.0041884150943396,53,49.80096,50.19986,,0,,,-4254.24152242083932392196,8228086,8223839,
	2025-08-21T00:00:00Z,,03c6bd01-9241-4771-ad3c-a1d5eb06b68a,101,con/1,1289.5,2,1158,1421,5.6555533,2,5.000019,6.3110876,228.477535,2,225.31104,231.64403,49.82638,2,49.800034,49.852726,,0,,,8.44152242083932392196,8223839,8223848,
	```

=== "JSON Output"

	```json
	{
		"success":true,
		"meta":[
			{"streamId":"03c6bd01-9241-4771-ad3c-a1d5eb06b68a","zone":"Pacific/Auckland","kind":"n","objectId":101,"sourceId":"con/1","i":["watts","current","voltage","frequency","pcmLimit"],"a":["wattHours"]}
		],
		"data":[
			[0,[1755723600000,null],[2473.9038461538461538,52,1153,3518],[10.7490174230769231,52,5.0912933,14.998348],[230.1490155769230769,52,225.00418,234.99796],[49.9755515576923077,52,49.800114,50.199997],[null,0,null,null],[2078.5,8186363,8225742]],
			[0,[1755727200000,null],[2374.8166666666666667,60,1130,3501],[10.3080711733333333,60,5.0031877,14.948236],[230.273054,60,225.00723,234.97006],[50.0040209666666667,60,49.800064,50.2],[null,0,null,null],[2347.3,8225742,8228086]],
			[0,[1755730800000,null],[2570.4528301886792453,53,1154,3520],[11.1705456301886792,53,5.083911,14.998905],[230.085467358490566,53,225.00075,234.99974],[50.0041884150943396,53,49.80096,50.19986],[null,0,null,null],[-4254.24152242083932392196,8228086,8223839]],
			[0,[1755734400000,null],[1289.5,2,1158,1421],[5.6555533,2,5.000019,6.3110876],[228.477535,2,225.31104,231.64403],[49.82638,2,49.800034,49.852726],[null,0,null,null],[8.44152242083932392196,8223839,8223848]]
		]
	}
	```

Show a reading difference between two dates:

=== "Show reading difference"

	```sh
	s10k --profile demo datum list --node-id 101 --source-id con/1 \
	  --min-date 2025-08-21 --max-date 2025-08-22  --reading Difference
	```

=== "Pretty Output"

	```
	+----------------------+----------------------+-----------+-----------+-----------+
	| Timestamp            | Timestamp End        | Object ID | Source ID | wattHours |
	+----------------------+----------------------+-----------+-----------+-----------+
	| 2025-07-25T20:18:21Z | 2025-08-21T00:08:18Z | 101       | con/1     |     37485 |
	+----------------------+----------------------+-----------+-----------+-----------+
	```

=== "CSV Output"

	```csv
	ts_start,ts_end,streamId,objectId,sourceId,watts,watts_count,watts_min,watts_max,current,current_count,current_min,current_max,voltage,voltage_count,voltage_min,voltage_max,frequency,frequency_count,frequency_min,frequency_max,pcmLimit,pcmLimit_count,pcmLimit_min,pcmLimit_max,wattHours,wattHours_start,wattHours_end,tags
	2025-07-25T20:18:21Z,2025-08-21T00:08:18Z,03c6bd01-9241-4771-ad3c-a1d5eb06b68a,101,con/1,,,,,,,,,,,,,,,,,,,,,37485,8186363,8223848,
	```

=== "JSON Output"

	```json
	{
		"success":true,
		"meta":[
			{"streamId":"03c6bd01-9241-4771-ad3c-a1d5eb06b68a","zone":"Pacific/Auckland","kind":"n","objectId":101,"sourceId":"con/1","i":["watts","current","voltage","frequency","pcmLimit"],"a":["wattHours"]}
		],
		"data":[
			[0,[1753474701000,1755734898000],null,null,null,null,null,[37485,8186363,8223848]]
		]
	}
	```


[aggregation]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarQuery-API-enumerated-types#aggregation-types
[partial-aggregation]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-aggregation#list-partial-aggregation
[reading]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarQuery-API-enumerated-types#datum-reading-types
[stream-list]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarQuery-Stream-API#datum-stream-datum-list
[stream-reading]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarQuery-Stream-API#datum-stream-reading-list
