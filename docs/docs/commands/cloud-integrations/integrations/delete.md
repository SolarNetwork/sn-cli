---
title: delete
---
# Cloud Integration Delete

Delete [Cloud Integration][integration] entities. All entities associated with any deleted
integration will be deleted as well.

## Usage

```
s10k cloud-integrations integrations delete
	[-i=integrationId[,integrationId...]]...
    [-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-i=`   | `--integration-id=` | the integration ID(s) to match |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to simulate what entities
	would be deleted, without actually deleting anything. For example:

	```sh
	s10k --dry-run cloud-integrations integrations delete --integration-id 100
	```

## Output

A listing of the deleted datum streams.

## Examples

=== "Delete datum streams"

	```sh
	s10k cloud-integrations integrations delete --integration-id 99
	```

=== "Delete datum streams (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `i9n` instead of `integrations`:

	```sh
	s10k c2c i9n delete --integration-id 99
	```

=== "Pretty Output"

	```
	+----+--------------------------+-----------+---------+
	| ID | Name                     | Type      | Enabled |
	+----+--------------------------+-----------+---------+
	| 99 | John Smither High School | SolarEdge | true    |
	+----+--------------------------+-----------+---------+
	```

=== "CSV Output"

	```csv
	ID,Name,Type,Enabled
	99,John Smither High School,SolarEdge,true
	```

=== "JSON Output"

	```json
	[
	  {
	    "configId": 99,
	    "name": "John Smither High School",
	    "serviceIdentifier": "s10k.c2c.i9n.solaredge.v1",
	    "created": "2024-11-07 13:23:18.783834Z",
	    "modified": "2024-11-07 13:23:18.783834Z",
	    "enabled": true,
	    "serviceProperties": {
	      "apiKey": "{SSHA-256}REtLiAO1cMh541Dr...=="
	    }
	  }
	]
	```


[integration]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-integration
