---
title: data-values
---
# Cloud Datum Stream Data Values

Discover the supported cloud data values for an integration. The data values represent the
properties that can be mapped into a cloud datum stream.

## Usage

```
s10k cloud-integrations datum-streams data-values
	-i=<integrationId>
	[-p=<path>]
	[-t=<type> | -stream=<datumStreamId>]
	[-mode=<displayMode>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-i=` | `--integration-id=` | the Cloud Integration ID to list data values for |
| `-p=` | `--path=` | the [data hierarchy path](#data-hierarchy-paths) to show |
| `-stream=` | `--stream-id=` | the datum stream ID to list data value for; only required by some providers, like eGauge |
| `-t=` | `--stream-type=` | a datum stream service identifier, required when `--path` provided; a case-insensitive sub-string match is performed against both the service identifier and the display name, for example `also` will match the AlsoEnergy type; not needed if `--stream-id` option is provided |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A list of data value records. Each data value record contains the following properties:

| Property        | Description |
|:----------------|:------------|
| **Name**        | A display name for the data value. |
| **Identifiers** | An ordered list of the unique hierarchy identifiers that the data value represents. |
| **Reference**   | The mapping reference, if the data value can be used in Cloud Datum Stream property mapping. |
| **Metadata**    | Optional table of provider-specific metadata, such as device serial number or location. |
| **Children**    | Data values are presented in a hierarchy, and if a data value has nested data values they appear as children. |

!!! note "Children are flattened in PRETTY and CSV display mode"

	When the display mode is `PRETTY` or `CSV` all nested children data values are presented
	as top-level rows, not as a nested hierarchy. You can tell where in the hierarchy any
	particular row actually resides by looking at the **Identifiers** value.

## Data hierarchy paths

When you run this command without any `--path` option you will typically get back a list of
"available sites" supported by the given integration. Each "site" record will have an **Identifier**
value (or values). To inspect the details of a particular "site" you must pass in the data value
path for that site, which is a URL-like path of that site's **Identifier** values.

When list of data value records for a "site" you will typically get back a list of "devices"
available on that site. Each "device" record will its own Identifier values and you can use those to
construct a deeper `--path` value and then discover the  mappable properties available on that
device, as data value records with **Reference** values.

Here are some examples of how Identifier lists can be represented as paths:

| Identifiers | Path |
|:------------|:-----|
| `1111111`   | `1111111` |
| `1111111`, `22222` | `1111111/22222` |
| `1111111`, `22222` | `/1111111/22222` <small>a leading `/` character is allowed</small> |
| `1111111`, `met`, `ABC123-DF` | `1111111/met/ABC123-DF` |

## Examples

### 1st level

Here is an example of a 1st level _available sites_ style query:

=== "1st level query"

	```sh
	s10k cloud-integrations datum-streams data-values --integration-id 100
	```

=== "1st level query (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams`
	and `dv` instead of `data-values`:

	```sh
	s10k c2c ds dv --integration-id 100
	```

=== "Pretty Output"

	```
	+-----------------------+-------------+-----------+--------------------------------+
	| Name                  | Identifiers | Reference | Metadata                       |
	+-----------------------+-------------+-----------+--------------------------------+
	| 123 E. Main Street    | 1111111     |           | status     Active              |
	|                       |             |           | street     123 E. Main Street  |
	|                       |             |           | l          Tracy               |
	|                       |             |           | st         California          |
	|                       |             |           | c          United States       |
	|                       |             |           | postalCode 95304               |
	|                       |             |           | tz         America/Los_Angeles |
	|                       |             |           |                                |
	+-----------------------+-------------+-----------+--------------------------------+
	```

=== "CSV Output"

	```csv
	Name,Identifiers,Reference,Metadata
	123 E. Main Street,1111111,,"status     Active
	street     123 E. Main Street
	l          Tracy
	st         California
	c          United States
	postalCode 95304
	tz         America/Los_Angeles
	"
	```

=== "JSON Output"

	```json
	[
		{
			"name" : "123 E. Main Street",
			"identifiers" :   [ "1111111" ],
			"metadata" : {
				"status" : "Active",
				"street" : "123 E. Main Street",
				"l" : "Tracy",
				"st" : "California",
				"c" : "United States",
				"postalCode" : "95304",
				"tz" : "America/Los_Angeles"
			}
		}
	]
	```

### 2nd level

Here is an example of a 2nd level _available devices_ style query. We turn the **Identifier**
from the previous 1st level query into a `--path` option:

=== "2nd-level query"

	```sh
	s10k cloud-integrations datum-streams data-values --integration-id 100 \
	     --stream-type solaredge --path 1111111
	```

=== "2nd level query (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams`
	and `dv` instead of `data-values`:

	```sh
	s10k c2c ds dv --integration-id 100 --stream-type solaredge --path 1111111
	```

=== "Pretty Output"

	```
	+------------+-------------+-----------+-----------------------------------+
	| Name       | Identifiers | Reference | Metadata                          |
	+------------+-------------+-----------+-----------------------------------+
	| Inverters  | 1111111     |           |                                   |
	|            | inv         |           |                                   |
	+------------+-------------+-----------+-----------------------------------+
	| Inverter 3 | 1111111     |           | serial          BBBBBBBB-08       |
	|            | inv         |           | manufacturer    SolarEdge         |
	|            | BBBBBBBB-08 |           | model           SE66.6K-USRP0BNU4 |
	|            |             |           | firmwareVersion CPU: 4.21.515     |
	|            |             |           |                                   |
	+------------+-------------+-----------+-----------------------------------+
	| Inverter 2 | 1111111     |           | serial          DDDDDDDD-4E       |
	|            | inv         |           | manufacturer    SolarEdge         |
	|            | DDDDDDDD-4E |           | model           SE100K-USRP0BNU4  |
	|            |             |           | firmwareVersion CPU: 4.21.515     |
	|            |             |           |                                   |
	+------------+-------------+-----------+-----------------------------------+
	| Inverter 1 | 1111111     |           | serial          EEEEEEEE-D0       |
	|            | inv         |           | manufacturer    SolarEdge         |
	|            | EEEEEEEE-D0 |           | model           SE100K-USRP0BNU4  |
	|            |             |           | firmwareVersion CPU: 4.21.515     |
	|            |             |           |                                   |
	+------------+-------------+-----------+-----------------------------------+
	```

=== "CSV Output"

	```csv
	Name,Identifiers,Reference,Metadata
	Inverters,"1111111
	inv",,
	Inverter 3,"1111111
	inv
	BBBBBBBB-08",,"serial          BBBBBBBB-08
	manufacturer    SolarEdge
	model           SE66.6K-USRP0BNU4
	firmwareVersion CPU: 4.21.515
	"
	Inverter 2,"1111111
	inv
	DDDDDDDD-4E",,"serial          DDDDDDDD-4E
	manufacturer    SolarEdge
	model           SE100K-USRP0BNU4
	firmwareVersion CPU: 4.21.515
	"
	Inverter 1,"1111111
	inv
	EEEEEEEE-D0",,"serial          EEEEEEEE-D0
	manufacturer    SolarEdge
	model           SE100K-USRP0BNU4
	firmwareVersion CPU: 4.21.515
	"
	```

=== "JSON Output"

	```json
	[
		{
			"name" : "Inverters",
			"identifiers" :   [ "1111111", "inv" ],
			"children" :   [
				{
					"name" : "Inverter 3",
					"identifiers" : [ "1111111", "inv", "BBBBBBBB-08" ],
					"metadata" : {
						"serial" : "BBBBBBBB-08",
						"manufacturer" : "SolarEdge",
						"model" : "SE66.6K-USRP0BNU4",
						"firmwareVersion" : "CPU: 4.21.515"
					}
				}, {
					"name" : "Inverter 2",
					"identifiers" : [ "1111111", "inv", "DDDDDDDD-4E" ],
					"metadata" : {
						"serial" : "DDDDDDDD-4E",
						"manufacturer" : "SolarEdge",
						"model" : "SE100K-USRP0BNU4",
						"firmwareVersion" : "CPU: 4.21.515"
					}
				}, {
					"name" : "Inverter 1",
					"identifiers" : [ "1111111", "inv", "EEEEEEEE-D0" ],
					"metadata" : {
						"serial" : "EEEEEEEE-D0",
						"manufacturer" : "SolarEdge",
						"model" : "SE100K-USRP0BNU4",
						"firmwareVersion" : "CPU: 4.21.515"
					}
				}
			]
		}
	]
	```

### 3rd level

Here is an example of a 3rd-level _device properties_ style query. We turn the first **Identifier**
from the previous 2nd level query into a `--path` option. Here you can see **Reference** values
populated, which means they can be used in a Cloud Datum Stream property mapping:

=== "3rd-level query"

	```sh
	s10k cloud-integrations datum-streams data-values --integration-id 100 \
	     --stream-type solaredge --path 1111111/inv/BBBBBBBB-08
	```

=== "3rd-level query (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams`
	and `dv` instead of `data-values`:

	```sh
	s10k c2c ds dv --integration-id 100 --stream-type solaredge --path 1111111/inv/BBBBBBBB-08
	```

=== "Pretty Output"

	```
	+--------------------------+-------------+-----------------------------------+----------+
	| Name                     | Identifiers | Reference                         | Metadata |
	+--------------------------+-------------+-----------------------------------+----------+
	| DC voltage               | 1111111     | /1111111/inv/BBBBBBBB-08/DCV      |          |
	|                          | inv         |                                   |          |
	|                          | BBBBBBBB-08 |                                   |          |
	|                          | DCV         |                                   |          |
	+--------------------------+-------------+-----------------------------------+----------+
	| Total energy             | 1111111     | /1111111/inv/BBBBBBBB-08/TotWhExp |          |
	|                          | inv         |                                   |          |
	|                          | BBBBBBBB-08 |                                   |          |
	|                          | TotWhExp    |                                   |          |
	+--------------------------+-------------+-----------------------------------+----------+
	| Total active power       | 1111111     | /1111111/inv/BBBBBBBB-08/W        |          |
	|                          | inv         |                                   |          |
	|                          | BBBBBBBB-08 |                                   |          |
	|                          | W           |                                   |          |
	+--------------------------+-------------+-----------------------------------+----------+
	```

=== "CSV Output"

	```csv
	Name,Identifiers,Reference,Metadata
	DC voltage,"1111111
	inv
	BBBBBBBB-08
	DCV",/1111111/inv/BBBBBBBB-08/DCV,
	Total energy,"1111111
	inv
	BBBBBBBB-08
	TotWhExp",/1111111/inv/BBBBBBBB-08/TotWhExp,
	Total active power,"1111111
	inv
	BBBBBBBB-08
	W",/1111111/inv/BBBBBBBB-08/W,
	```

=== "JSON Output"

	```json
	[
		{
			"name" : "DC voltage",
			"reference" : "/1111111/inv/BBBBBBBB-08/DCV",
			"identifiers" :   [ "1111111", "inv", "BBBBBBBB-08", "DCV" ]
		},
		{
			"name" : "Total energy",
			"reference" : "/1111111/inv/BBBBBBBB-08/TotWhExp",
			"identifiers" :   [ "1111111", "inv", "BBBBBBBB-08", "TotWhExp" ]
		},
		{
			"name" : "Total active power",
			"reference" : "/1111111/inv/BBBBBBBB-08/W",
			"identifiers" :   [ "1111111", "inv", "BBBBBBBB-08", "W" ]
		}
	]
	```
