---
title: list
---
# Cloud Datum Stream Mapping List

Show [Cloud Datum Stream Mapping][mapping] entities matching a search filter, along with their
associated [Integration][integration] and [Mapping Property][mapping-prop] entities.

## Usage

```
s10k cloud-integrations mappings list
	[-i=integrationId[,integrationId...]]...
	[-map=mappingId[,mappingId...]]...
    [-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-i=`   | `--integration-id=` | the integration ID(s) to match |
| `-map=` | `--mapping-id=` | the datum stream mapping ID(s) to match |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of matching mappings and their associated integrations and properties.

## Examples

=== "List mappings"

	```sh
	s10k cloud-integrations mappings list
	```

=== "List mappings (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `maps` instead of `mappings`:

	```sh
	s10k c2c maps list
	```

=== "Pretty Output"

	```
	╔═════╤═══════════════════════╤════════════════╤══════════════════╤═════════════════════╤════════════╤══════════════════╤═══════════════╤═════════════════╤════════════════╤════════════════════════════════════════════════════════════════════════════════╤════════════╤═══════╗
	║ ID  │ Name                  │ Integration ID │ Integration Name │ Integration Enabled │ Property # │ Property Enabled │ Property Type │ Property Name   │ Value Type     │ Value Reference                                                                │ Multiplier │ Scale ║
	╠═════╪═══════════════════════╪════════════════╪══════════════════╪═════════════════════╪════════════╪══════════════════╪═══════════════╪═════════════════╪════════════════╪════════════════════════════════════════════════════════════════════════════════╪════════════╪═══════╣
	║ 111 │ 222 - Solcast - S1    │            222 │ Solcast          │ true                │          0 │ true             │ Instantaneous │ irradiance      │ Reference      │ /GHI                                                                           │            │       ║
	╟─────┼───────────────────────┼────────────────┼──────────────────┼─────────────────────┼────────────┼──────────────────┼───────────────┼─────────────────┼────────────────┼────────────────────────────────────────────────────────────────────────────────┼────────────┼───────╢
	║     │                       │                │                  │                     │          1 │ true             │ Accumulating  │ irradianceHours │ SpelExpression │ hasOffset(1, timestamp) && offset(1, timestamp).props['irradianceHours'] !=    │            │       ║
	║     │                       │                │                  │                     │            │                  │               │                 │                │ null ? offset(1, timestamp).irradianceHours + round( (secondsBetween(offset(1, │            │       ║
	║     │                       │                │                  │                     │            │                  │               │                 │                │ timestamp).timestamp, timestamp) / 3600.0) * avg({offset(1,                    │            │       ║
	║     │                       │                │                  │                     │            │                  │               │                 │                │ timestamp).irradiance, irradiance})) : 0                                       │            │       ║
	╟─────┼───────────────────────┼────────────────┼──────────────────┼─────────────────────┼────────────┼──────────────────┼───────────────┼─────────────────┼────────────────┼────────────────────────────────────────────────────────────────────────────────┼────────────┼───────╢
	║ 888 │ 999 - AlsoEnergy - S1 │            999 │ Powertrack       │ true                │          0 │ true             │ Instantaneous │ watts           │ Reference      │ /{siteId}/{hardwareId}/KW/Last                                                 │       1000 │     0 ║
	╟─────┼───────────────────────┼────────────────┼──────────────────┼─────────────────────┼────────────┼──────────────────┼───────────────┼─────────────────┼────────────────┼────────────────────────────────────────────────────────────────────────────────┼────────────┼───────╢
	║     │                       │                │                  │                     │          1 │ true             │ Accumulating  │ wattHours       │ Reference      │ /{siteId}/{hardwareId}/KWHnet/Last                                             │       1000 │     0 ║
	╟─────┼───────────────────────┼────────────────┼──────────────────┼─────────────────────┼────────────┼──────────────────┼───────────────┼─────────────────┼────────────────┼────────────────────────────────────────────────────────────────────────────────┼────────────┼───────╢
	║     │                       │                │                  │                     │          2 │ true             │ Instantaneous │ frequency       │ Reference      │ /{siteId}/{hardwareId}/Frequency/Last                                          │            │       ║
	╟─────┼───────────────────────┼────────────────┼──────────────────┼─────────────────────┼────────────┼──────────────────┼───────────────┼─────────────────┼────────────────┼────────────────────────────────────────────────────────────────────────────────┼────────────┼───────╢
	║     │                       │                │                  │                     │          3 │ true             │ Instantaneous │ watts           │ Reference      │ /{siteId}/{hardwareId}/KwAC/Last                                               │       1000 │     0 ║
	╟─────┼───────────────────────┼────────────────┼──────────────────┼─────────────────────┼────────────┼──────────────────┼───────────────┼─────────────────┼────────────────┼────────────────────────────────────────────────────────────────────────────────┼────────────┼───────╢
	║     │                       │                │                  │                     │          4 │ true             │ Accumulating  │ wattHours       │ Reference      │ /{siteId}/{hardwareId}/KwhAC/Last                                              │       1000 │     0 ║
	╚═════╧═══════════════════════╧════════════════╧══════════════════╧═════════════════════╧════════════╧══════════════════╧═══════════════╧═════════════════╧════════════════╧════════════════════════════════════════════════════════════════════════════════╧════════════╧═══════╝
	```

=== "CSV Output"

	```csv
	ID,Name,Integration ID,Integration Name,Integration Enabled,Property #,Property Enabled,Property Type,Property Name,Value Type,Value Reference,Multiplier,Scale
	111,222 - Solcast - S1,222,Solcast,true,0,true,Instantaneous,irradiance,Reference,/GHI,,
	,,,,,1,true,Accumulating,irradianceHours,SpelExpression,"hasOffset(1, timestamp) && offset(1, timestamp).props['irradianceHours'] != null ? offset(1, timestamp).irradianceHours + round( (secondsBetween(offset(1, timestamp).timestamp, timestamp) / 3600.0) * avg({offset(1, timestamp).irradiance, irradiance})) : 0",,
	888,999 - AlsoEnergy - S1,999,Powertrack,true,0,true,Instantaneous,watts,Reference,/{siteId}/{hardwareId}/KW/Last,1000,0
	,,,,,1,true,Accumulating,wattHours,Reference,/{siteId}/{hardwareId}/KWHnet/Last,1000,0
	,,,,,2,true,Instantaneous,frequency,Reference,/{siteId}/{hardwareId}/Frequency/Last,,
	,,,,,3,true,Instantaneous,watts,Reference,/{siteId}/{hardwareId}/KwAC/Last,1000,0
	,,,,,4,true,Accumulating,wattHours,Reference,/{siteId}/{hardwareId}/KwhAC/Last,1000,0
	```

=== "JSON Output"

	```json
	[
	  {
	    "mapping": {
	      "configId": 111,
	      "name": "222 - Solcast - S1",
	      "created": "2026-06-16 03:50:52.020614Z",
	      "modified": "2026-06-16 03:50:52.020614Z",
	      "integrationId": 222
	    },
	    "integration": {
	      "configId": 222,
	      "name": "Solcast",
	      "serviceIdentifier": "s10k.c2c.i9n.solcast",
	      "created": "2026-06-16 03:35:02.375952Z",
	      "modified": "2026-06-16 03:35:02.375952Z",
	      "enabled": true,
	      "serviceProperties": {
	        "apiKey": "{SSHA-256}srrz9qQvDYjamimjTkEQO+lvE0TM2uRUxL3AgJBj++G7rsWJ4FzGhA=="
	      }
	    },
	    "properties": [
	      {
	        "datumStreamMappingId": 111,
	        "index": 0,
	        "created": "2026-06-16 03:50:52.364119Z",
	        "modified": "2026-06-16 03:50:52.364119Z",
	        "enabled": true,
	        "propertyType": "i",
	        "propertyName": "irradiance",
	        "valueType": "r",
	        "valueReference": "/GHI"
	      },
	      {
	        "datumStreamMappingId": 111,
	        "index": 1,
	        "created": "2026-06-16 03:50:52.364119Z",
	        "modified": "2026-06-16 03:50:52.364119Z",
	        "enabled": true,
	        "propertyType": "a",
	        "propertyName": "irradianceHours",
	        "valueType": "s",
	        "valueReference": "hasOffset(1, timestamp) && offset(1, timestamp).props['irradianceHours'] != null ? offset(1, timestamp).irradianceHours + round( (secondsBetween(offset(1, timestamp).timestamp, timestamp) / 3600.0) * avg({offset(1, timestamp).irradiance, irradiance})) : 0"
	      }
	    ]
	  },
	  {
	    "mapping": {
	      "configId": 888,
	      "name": "999 - AlsoEnergy - S1",
	      "created": "2026-06-26 08:32:56.993666Z",
	      "modified": "2026-06-26 08:32:56.993666Z",
	      "integrationId": 999
	    },
	    "integration": {
	      "configId": 999,
	      "name": "Powertrack",
	      "serviceIdentifier": "s10k.c2c.i9n.also",
	      "created": "2026-06-17 19:15:02.203522Z",
	      "modified": "2026-06-17 19:15:02.203522Z",
	      "enabled": true,
	      "serviceProperties": {
	        "password": "{SSHA-256}oQFMC5Snij/FB6cu/0OQ2WeNpkRqmxseHvfrhY+wc20w6J8TNzi1NA==",
	        "username": "neon-support+transformenergy@ecosuite.io",
	        "oauthClientId": "also216"
	      }
	    },
	    "properties": [
	      {
	        "datumStreamMappingId": 888,
	        "index": 0,
	        "created": "2026-06-26 08:33:01.365174Z",
	        "modified": "2026-06-26 08:33:01.365174Z",
	        "enabled": true,
	        "propertyType": "i",
	        "propertyName": "watts",
	        "valueType": "r",
	        "valueReference": "/{siteId}/{hardwareId}/KW/Last",
	        "multiplier": 1000,
	        "scale": 0
	      },
	      {
	        "datumStreamMappingId": 888,
	        "index": 1,
	        "created": "2026-06-26 08:33:01.365174Z",
	        "modified": "2026-06-26 08:33:01.365174Z",
	        "enabled": true,
	        "propertyType": "a",
	        "propertyName": "wattHours",
	        "valueType": "r",
	        "valueReference": "/{siteId}/{hardwareId}/KWHnet/Last",
	        "multiplier": 1000,
	        "scale": 0
	      },
	      {
	        "datumStreamMappingId": 888,
	        "index": 2,
	        "created": "2026-06-26 08:33:01.365174Z",
	        "modified": "2026-06-26 08:33:01.365174Z",
	        "enabled": true,
	        "propertyType": "i",
	        "propertyName": "frequency",
	        "valueType": "r",
	        "valueReference": "/{siteId}/{hardwareId}/Frequency/Last"
	      },
	      {
	        "datumStreamMappingId": 888,
	        "index": 3,
	        "created": "2026-06-26 08:33:01.365174Z",
	        "modified": "2026-06-26 08:33:01.365174Z",
	        "enabled": true,
	        "propertyType": "i",
	        "propertyName": "watts",
	        "valueType": "r",
	        "valueReference": "/{siteId}/{hardwareId}/KwAC/Last",
	        "multiplier": 1000,
	        "scale": 0
	      },
	      {
	        "datumStreamMappingId": 888,
	        "index": 4,
	        "created": "2026-06-26 08:33:01.365174Z",
	        "modified": "2026-06-26 08:33:01.365174Z",
	        "enabled": true,
	        "propertyType": "a",
	        "propertyName": "wattHours",
	        "valueType": "r",
	        "valueReference": "/{siteId}/{hardwareId}/KwhAC/Last",
	        "multiplier": 1000,
	        "scale": 0
	      }
	    ]
	  }
	]
	```


[integration]: https://github.com/SolarNetwork/solarnetwork/wiki/Cloud-Integrations#cloud-integration-entity
[mapping]: https://github.com/SolarNetwork/solarnetwork/wiki/Cloud-Integrations#cloud-datum-stream-mapping-entity
[mapping-prop]: https://github.com/SolarNetwork/solarnetwork/wiki/Cloud-Integrations#cloud-datum-stream-mapping-property-entity
