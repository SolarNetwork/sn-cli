---
title: delete
---
# Cloud Datum Stream Delete

Delete [Cloud Datum Stream][datum-stream] entities. All associated Poll and Rake Task entities will
be deleted as well.

## Usage

```
s10k cloud-integrations datum-streams delete
	[-stream=datumStreamId[,datumStreamId...]]...
    [-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-stream=` | `--stream-id=` | the datum stream ID(s) to match; any ID that does not match an existing entity will be ignored |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to simulate what entities would be deleted,
	without actually deleting anything. For example:

	```sh
	s10k --dry-run cloud-integrations datum-streams delete --stream-id 100
	```

## Output

A listing of the deleted datum streams.

## Examples

=== "Delete datum streams"

	```sh
	s10k cloud-integrations datum-streams delete --stream-id 9
	```

=== "Delete datum streams (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams`:

	```sh
	s10k c2c ds delete --stream-id 9
	```

=== "Pretty Output"

	```
	+----+-------------------+-----------+---------+------+-----------+-------------------+----------+------------+------------------------+
	| ID | Name              | Type      | Enabled | Kind | Object ID | Source ID         | Schedule | Mapping ID | Service Properties     |
	+----+-------------------+-----------+---------+------+-----------+-------------------+----------+------------+------------------------+
	|  9 | A Solar Farm Site | SolarEdge | true    | n    |       123 | site/solaredge-v1 | 900      |          4 | {                      |
	|    |                   |           |         |      |           |                   |          |            |   "placeholders" : {   |
	|    |                   |           |         |      |           |                   |          |            |     "siteId" : 0000000 |
	|    |                   |           |         |      |           |                   |          |            |   }                    |
	|    |                   |           |         |      |           |                   |          |            | }                      |
	+----+-------------------+-----------+---------+------+-----------+-------------------+----------+------------+------------------------+
	```

=== "CSV Output"

	```csv
	ID,Name,Type,Enabled,Kind,Object ID,Source ID,Schedule,Mapping ID,Service Properties
	9,A Solar Farm Site,SolarEdge,true,n,123,site/solaredge-v1,900,4,"{
	""placeholders"" : {
		""siteId"" : 0000000
	}
	}"
	```

=== "JSON Output"

	```json
	[
	  {
	    "configId": 4,
	    "name": "A Solar Farm Site",
	    "serviceIdentifier": "s10k.c2c.ds.solaredge.v1",
	    "created": "2024-10-24 04:40:14.538415Z",
	    "modified": "2024-10-24 04:40:14.538415Z",
	    "enabled": true,
	    "datumStreamMappingId": 4,
	    "schedule": "900",
	    "kind": "n",
	    "objectId": 123,
	    "sourceId": "site/solaredge-v1",
	    "serviceProperties": {
	      "placeholders": {
	        "siteId": 0000000
	      }
	    }
	  }
	]
	```


[datum-stream]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream
