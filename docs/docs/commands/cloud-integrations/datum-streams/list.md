---
title: list
---
# Cloud Datum Stream List

Show [Cloud Datum Stream][datum-stream] entities matching a search filter.

## Usage

```
s10k cloud-integrations datum-streams list
	[-stream=datumStreamId[,datumStreamId...]]...
	[-source=sourceId[,sourceId...]]...
    [-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-source=` | `--source=` | the source ID(s) to restrict the results to |
| `-stream=` | `--stream-id=` | the datum stream ID(s) to restrict the results to |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of matching datum streams.

## Examples

=== "List datum streams"

	```sh
	s10k cloud-integrations datum-streams list
	```

=== "List datum streams (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams`:

	```sh
	s10k c2c ds list
	```

=== "Pretty Output"

	```
	+------+----------------------------+-------------------+---------+------+-----------+--------------------+----------------+------------+
	| ID   | Name                       | Type              | Enabled | Kind | Object ID | Source ID          | Schedule       | Mapping ID |
	+------+----------------------------+-------------------+---------+------+-----------+--------------------+----------------+------------+
	| 1000 | Solar Farm S1F2 weather    | Solcast (Weather) | true    | n    |       123 | /S1F2/S1/B1/PYR/1  | 0 0/30 * * * * |        111 |
	+------+----------------------------+-------------------+---------+------+-----------+--------------------+----------------+------------+
	| 1001 | Solar Farm S1F2 PV         | AlsoEnergy        | true    | n    |       123 | /S1F2/S1/B1/GEN/1  | 0 0/30 * * * * |        222 |
	|      |                            |                   |         |      |           | /S1F2/S1/B1/INV/1  |                |            |
	|      |                            |                   |         |      |           | /S1F2/S1/B1/INV/2  |                |            |
	+------+----------------------------+-------------------+---------+------+-----------+--------------------+----------------+------------+
	```

=== "CSV Output"

	```csv
	ID,Name,Type,Enabled,Kind,Object ID,Source ID,Schedule,Mapping ID
	1000,Solar Farm S1F2 weather,Solcast (Weather),true,n,123,/S1F2/S1/B1/PYR/1,0 0/30 * * * *,111
	1001,Solar Farm S1F2 PV,AlsoEnergy,true,n,123,"/S1F2/S1/B1/GEN/1
	/S1F2/S1/B1/INV/1
	/S1F2/S1/B1/INV/2",0 0/30 * * * *,222
	```

=== "JSON Output"

	```json
	[
		  {
			"configId": 1000,
			"name": "Solar Farm S1F2 weather",
			"serviceIdentifier": "s10k.c2c.ds.solcast.irr",
			"created": "2026-06-16 04:20:22.973673Z",
			"modified": "2026-06-16 04:20:22.973673Z",
			"enabled": true,
			"datumStreamMappingId": 111,
			"schedule": "0 0/30 * * * *",
			"kind": "n",
			"objectId": 123,
			"sourceId": "/S1F2/S1/B1/PYR/1",
			"serviceProperties": {
				"lat": 38.66,
				"lon": -121.735
			}
		},
		{
			"configId": 1001,
			"name": "Solar Farm S1F2 PV",
			"serviceIdentifier": "s10k.c2c.ds.also",
			"created": "2026-06-17 19:20:20.809666Z",
			"modified": "2026-06-17 19:24:21.585026Z",
			"enabled": true,
			"datumStreamMappingId": 222,
			"schedule": "0 0/30 * * * *",
			"kind": "n",
			"objectId": 123,
			"sourceId": "unused",
			"serviceProperties": {
				"sourceIdMap": {
					"/40000/30000": "/S1F2/S1/B1/GEN/1",
					"/40000/30001": "/S1F2/S1/B1/INV/1",
					"/40000/30002": "/S1F2/S1/B1/INV/2"
				}
			}
		}
	]
	```


[datum-stream]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream
