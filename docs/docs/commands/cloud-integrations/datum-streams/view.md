---
title: view
---
# Cloud Datum Stream View

Show detailed information about a [Cloud Datum Stream][datum-stream] entity, including its mapping,
integration, and mapped property details. This is a good way to get a holistic picture of a datum
stream.

## Usage

```
s10k cloud-integrations datum-streams view
	-stream=<datumStreamId>
	[-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the datum stream ID to show information for |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

Detailed information about the datum stream, including its mapped properties.

## Examples

=== "View datum stream"

	```sh
	s10k cloud-integrations datum-streams view --stream-id 1000
	```

=== "View datum stream (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams`:

	```sh
	s10k c2c ds view --stream-id 1000
	```

=== "Pretty Output"

	```
	+------+--------------------+------------+---------+------+-----------+-------------------+----------------+------------+------------------+----------------+------------------+---------------------+------------+------------------+---------------+---------------+------------+------------------------------------+
	| ID   | Name               | Type       | Enabled | Kind | Object ID | Source ID         | Schedule       | Mapping ID | Mapping Name     | Integration ID | Integration Name | Integration Enabled | Property # | Property Enabled | Property Type | Property Name | Value Type | Value Reference                    |
	+------+--------------------+------------+---------+------+-----------+-------------------+----------------+------------+------------------+----------------+------------------+---------------------+------------+------------------+---------------+---------------+------------+------------------------------------+
	| 1000 | Solar Farm S1F2 PV | AlsoEnergy | true    | n    |       123 | /S1F2/S1/B1/GEN/1 | 0 0/30 * * * * |        222 | AlsoEnergy Basic |            121 | AlsoEnergy       | true                |          0 | true             | i             | watts         | r          | /{siteId}/{hardwareId}/KW/Last     |
	|      |                    |            |         |      |           | /S1F2/S1/B1/INV/1 |                |            |                  |                |                  |                     |            |                  |               |               |            |                                    |
	|      |                    |            |         |      |           | /S1F2/S1/B1/INV/2 |                |            |                  |                |                  |                     |            |                  |               |               |            |                                    |
	+------+--------------------+------------+---------+------+-----------+-------------------+----------------+------------+------------------+----------------+------------------+---------------------+------------+------------------+---------------+---------------+------------+------------------------------------+
	| 1000 | Solar Farm S1F2 PV | AlsoEnergy | true    | n    |       123 | /S1F2/S1/B1/GEN/1 | 0 0/30 * * * * |        222 | AlsoEnergy Basic |            121 | AlsoEnergy       | true                |          1 | true             | a             | wattHours     | r          | /{siteId}/{hardwareId}/KWHnet/Last |
	|      |                    |            |         |      |           | /S1F2/S1/B1/INV/1 |                |            |                  |                |                  |                     |            |                  |               |               |            |                                    |
	|      |                    |            |         |      |           | /S1F2/S1/B1/INV/1 |                |            |                  |                |                  |                     |            |                  |               |               |            |                                    |
	+------+--------------------+------------+---------+------+-----------+-------------------+----------------+------------+------------------+----------------+------------------+---------------------+------------+------------------+---------------+---------------+------------+------------------------------------+
	```

=== "CSV Output"

	```csv
	ID,Name,Type,Enabled,Kind,Object ID,Source ID,Schedule,Mapping ID,Mapping Name,Integration ID,Integration Name,Integration Enabled,Property #,Property Enabled,Property Type,Property Name,Value Type,Value Reference
	1000,Solar Farm S1F2 PV,AlsoEnergy,true,n,123,"/S1F2/S1/B1/GEN/1
	/S1F2/S1/B1/INV/1
	/S1F2/S1/B1/INV/2",0 0/30 * * * *,222,AlsoEnergy Basic,121,AlsoEnergy,true,0,true,i,watts,r,/{siteId}/{hardwareId}/KW/Last
	1000,Solar Farm S1F2 PV,AlsoEnergy,true,n,123,"/S1F2/S1/B1/GEN/1
	/S1F2/S1/B1/INV/1
	/S1F2/S1/B1/INV/2",0 0/30 * * * *,222,AlsoEnergy Basic,121,AlsoEnergy,true,1,true,a,wattHours,r,/{siteId}/{hardwareId}/KWHnet/Last
	```

=== "JSON Output"

	```json
	[
	  {
		"datumStream": {
		  "configId": 1000,
		  "name": "Solar Farm S1F2 PV",
		  "serviceIdentifier": "s10k.c2c.ds.also",
		  "created": "2026-06-18 19:24:31.562158Z",
		  "modified": "2026-06-18 19:25:08.053295Z",
		  "enabled": true,
		  "datumStreamMappingId": 222,
		  "schedule": "0 0/30 * * * *",
		  "kind": "n",
		  "objectId": 123,
		  "sourceId": "unused",
		  "serviceProperties": {
			"sourceIdMap": {
			  "/40000/100000": "/S1F2/S1/B1/GEN/1",
			  "/40000/100001": "/S1F2/S1/B1/INV/1",
			  "/40000/100002": "/S1F2/S1/B1/INV/2",
			}
		  }
		},
		"mapping": {
		  "configId": 222,
		  "name": "AlsoEnergy Basic",
		  "created": "2026-06-18 19:24:30.34634Z",
		  "modified": "2026-06-18 19:24:30.34634Z",
		  "integrationId": 121
		},
		"integration": {
		  "configId": 121,
		  "name": "AlsoEnergy",
		  "serviceIdentifier": "s10k.c2c.i9n.also",
		  "created": "2026-06-17 19:15:02.203522Z",
		  "modified": "2026-06-17 19:15:02.203522Z",
		  "enabled": true,
		  "serviceProperties": {
			"password": "{SSHA-256}DfAb0g+EdzIW/9/EIbbE38IzTtMEIIOqOawBlTLuwOlWHNOhpnlyqQ==",
			"username": "example@localhost"
		  }
		},
		"property": {
		  "datumStreamMappingId": 222,
		  "index": 0,
		  "created": "2026-06-22 01:09:28.523208Z",
		  "modified": "2026-06-22 01:09:28.523208Z",
		  "enabled": true,
		  "propertyType": "i",
		  "propertyName": "watts",
		  "valueType": "r",
		  "valueReference": "/{siteId}/{hardwareId}/KW/Last"
		}
	  },
	  {
		"datumStream": {
		  "configId": 1000,
		  "name": "Solar Farm S1F2 PV",
		  "serviceIdentifier": "s10k.c2c.ds.also",
		  "created": "2026-06-18 19:24:31.562158Z",
		  "modified": "2026-06-18 19:25:08.053295Z",
		  "enabled": true,
		  "datumStreamMappingId": 222,
		  "schedule": "0 0/30 * * * *",
		  "kind": "n",
		  "objectId": 123,
		  "sourceId": "unused",
		  "serviceProperties": {
			"sourceIdMap": {
			  "/40000/100000": "/S1F2/S1/B1/GEN/1",
			  "/40000/100001": "/S1F2/S1/B1/INV/1",
			  "/40000/100002": "/S1F2/S1/B1/INV/2",
			}
		  }
		},
		"mapping": {
		  "configId": 222,
		  "name": "AlsoEnergy Basic",
		  "created": "2026-06-18 19:24:30.34634Z",
		  "modified": "2026-06-18 19:24:30.34634Z",
		  "integrationId": 121
		},
		"integration": {
		  "configId": 121,
		  "name": "AlsoEnergy",
		  "serviceIdentifier": "s10k.c2c.i9n.also",
		  "created": "2026-06-17 19:15:02.203522Z",
		  "modified": "2026-06-17 19:15:02.203522Z",
		  "enabled": true,
		  "serviceProperties": {
			"password": "{SSHA-256}DfAb0g+EdzIW/9/EIbbE38IzTtMEIIOqOawBlTLuwOlWHNOhpnlyqQ==",
			"username": "example@localhost"
		  }
		},
		"property": {
		  "datumStreamMappingId": 222,
		  "index": 1,
		  "created": "2026-06-22 01:09:28.523208Z",
		  "modified": "2026-06-22 01:09:28.523208Z",
		  "enabled": true,
		  "propertyType": "a",
		  "propertyName": "wattHours",
		  "valueType": "r",
		  "valueReference": "/{siteId}/{hardwareId}/KWHnet/Last"
		}
	  }
	]
	```

!!! note "Repeated rows in CSV and PRETTY output"

	The CSV and PRETTY output will generate one row for each mapped property configured on the datum
	stream, and repeat all the stream/mapping/integration details on each row. The **Property #**
	column shows the property index value (unique across all properties within the datum stream).

[datum-stream]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream
