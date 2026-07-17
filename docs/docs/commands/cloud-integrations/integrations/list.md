---
title: list
---
# Cloud Integration List

Show [Cloud Integration][integration] entities matching a search filter.

## Usage

```
s10k cloud-integrations integrations list
	[-i=integrationId[,integrationId...]]...
	[-map=mappingId[,mappingId...]]...
	[-stream=datumStreamId[,datumStreamId...]]...
	[-m=name[,name...]]...
	[-S=serviceIdent[,serviceIdent...]]...
	[-e | -d]
    [-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-d`   | `--disabled` | match only disabled entities |
| `-e`   | `--enabled` | match only enabled entities |
| `-i=`   | `--integration-id=` | the integration ID(s) to match |
| `-m=`     | `--name=` | a case-insensitive name substring to match |
| `-map=` | `--mapping-id=` | the datum stream mapping ID(s) to match, by way of the integration relationship |
| `-S=` | `--service=` | the service idenetifier(s) to match |
| `-stream=` | `--stream-id=` | the datum stream ID(s) to match, by way of the mapping to datum stream relationship |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of matching integrations.

## Examples

=== "List integrations"

	```sh
	s10k cloud-integrations integrations list
	```

=== "List integrations (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `i9n` instead of `integrations`:

	```sh
	s10k c2c i9n list
	```

=== "Pretty Output"

	```
	+-----+------------+------------+---------+
	| ID  | Name       | Type       | Enabled |
	+-----+------------+------------+---------+
	| 123 | Solcast    | Solcast    | true    |
	+-----+------------+------------+---------+
	| 124 | Powertrack | AlsoEnergy | true    |
	+-----+------------+------------+---------+
	```

=== "CSV Output"

	```csv
	ID,Name,Type,Enabled
	123,Solcast,Solcast,true
	124,Powertrack,AlsoEnergy,true
	```

=== "JSON Output"

	```json
	[
		{
			"configId": 123,
			"name": "Solcast",
			"serviceIdentifier": "s10k.c2c.i9n.solcast",
			"created": "2026-06-16 03:35:02.375952Z",
			"modified": "2026-06-16 03:35:02.375952Z",
			"enabled": true,
			"serviceProperties": {
				"apiKey": "{SSHA-256}9aTrFh/rj33Wsnkjj5VqSvhFNNy5WEDbl238Ze/Vy37/1z9JjmXSw=="
			}
		},
		{
			"configId": 124,
			"name": "Powertrack",
			"serviceIdentifier": "s10k.c2c.i9n.also",
			"created": "2026-06-17 19:15:02.203522Z",
			"modified": "2026-06-17 19:15:02.203522Z",
			"enabled": true,
			"serviceProperties": {
				"password": "{SSHA-256}DfAb0g+EdzIW/9/EIbbE38IzTtMEIIOqOawBlTLuwOlWHNOhpnlyqQ==",
				"username": "example@localhost"
			}
		}
	]
	```


[integration]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-integration
