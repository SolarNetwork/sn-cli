---
title: delete
---
# Cloud Datum Stream Mapping Delete

Delete [Cloud Datum Stream Mapping][mapping] entities, along with associated [Cloud Datum Stream
Mapping Property][mapping-prop] entities.

## Usage

```
s10k cloud-integrations mappings delete
	[-map=mappingId[,mappingId...]]...
    [-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-map=` | `--mapping-id=` | the datum stream mapping ID(s) to delete |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to simulate what entities
	would be deleted, without actually deleting anything. For example:

	```sh
	s10k --dry-run cloud-integrations mappings delete --mapping-id 100
	```

## Output

A listing of the deleted datum stream mappings.

## Examples

=== "Delete mappings"

	```sh
	s10k cloud-integrations mappings delete --mapping-id 22
	```

=== "Delete mappings (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `maps` instead of `mappings`:

	```sh
	s10k c2c maps delete --mapping-id 22
	```

=== "Pretty Output"

	```
	╔════╤════════════╤════════════════╤═════════════════════╤═════════════════════╤════════════╤══════════════════╤═══════════════╤═══════════════╤════════════╤══════════════════════════╤════════════╤═══════╗
	║ ID │ Name       │ Integration ID │ Integration Name    │ Integration Enabled │ Property # │ Property Enabled │ Property Type │ Property Name │ Value Type │ Value Reference          │ Multiplier │ Scale ║
	╠════╪════════════╪════════════════╪═════════════════════╪═════════════════════╪════════════╪══════════════════╪═══════════════╪═══════════════╪════════════╪══════════════════════════╪════════════╪═══════╣
	║ 22 │ My Mapping │              7 │ SolarEdge - My Farm │ true                │          0 │ true             │ Instantaneous │ watts         │ Reference  │ /{siteId}/inv/*/W        │            │       ║
	╟────┼────────────┼────────────────┼─────────────────────┼─────────────────────┼────────────┼──────────────────┼───────────────┼───────────────┼────────────┼──────────────────────────┼────────────┼───────╢
	║    │            │                │                     │                     │          1 │ true             │ Accumulating  │ wattHours     │ Reference  │ /{siteId}/inv/*/TotWhExp │            │       ║
	╚════╧════════════╧════════════════╧═════════════════════╧═════════════════════╧════════════╧══════════════════╧═══════════════╧═══════════════╧════════════╧══════════════════════════╧════════════╧═══════╝
	```

=== "CSV Output"

	```csv
	ID,Name,Integration ID,Integration Name,Integration Enabled,Property #,Property Enabled,Property Type,Property Name,Value Type,Value Reference,Multiplier,Scale
	22,My Mapping,7,SolarEdge - My Farm,true,0,true,Instantaneous,watts,Reference,/{siteId}/inv/*/W,,
	,,,,,1,true,Accumulating,wattHours,Reference,/{siteId}/inv/*/TotWhExp,,
	```

=== "JSON Output"

	```json
	[
	  {
	    "configId": 22,
	    "name": "My Mapping",
	    "created": "2026-08-28 07:02:41.847056Z",
	    "modified": "2026-08-28 07:02:41.847056Z",
	    "integrationId": 7,
	    "integration": {
	      "configId": 7,
	      "name": "SolarEdge - My Farm",
	      "serviceIdentifier": "s10k.c2c.i9n.solaredge.v1",
	      "created": "2026-04-30 04:40:54.272321Z",
	      "modified": "2026-04-30 04:40:54.272321Z",
	      "enabled": true,
	      "serviceProperties": {
	        "apiKey": "{SSHA-256}hEJLil7Fy8QUAIAGfulv9LDenyQgBDUqFkD/lir3Eb+bQ/OvYxbnjA=="
	      }
	    },
	    "properties": [
	      {
	        "datumStreamMappingId": 22,
	        "index": 0,
	        "created": "2026-08-28 07:02:53.590294Z",
	        "modified": "2026-08-28 07:02:53.590294Z",
	        "enabled": true,
	        "propertyType": "i",
	        "propertyName": "watts",
	        "valueType": "r",
	        "valueReference": "/{siteId}/inv/*/W"
	      },
	      {
	        "datumStreamMappingId": 22,
	        "index": 1,
	        "created": "2026-08-28 07:02:53.590294Z",
	        "modified": "2026-08-28 07:02:53.590294Z",
	        "enabled": true,
	        "propertyType": "a",
	        "propertyName": "wattHours",
	        "valueType": "r",
	        "valueReference": "/{siteId}/inv/*/TotWhExp"
	      }
	    ]
	  }
	]
	```


[mapping]: https://github.com/SolarNetwork/solarnetwork/wiki/Cloud-Integrations#cloud-datum-stream-mapping-entity
[mapping-prop]: https://github.com/SolarNetwork/solarnetwork/wiki/Cloud-Integrations#cloud-datum-stream-mapping-property-entity
