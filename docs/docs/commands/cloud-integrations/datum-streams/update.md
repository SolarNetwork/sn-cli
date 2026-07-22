---
title: update
---
# Cloud Datum Stream Update

Make changes to an existing [Cloud Datum Stream][datum-stream] entity.

The changes to make can be provided by a combination of methods:

 1. Standard input, as a JSON object in the form supported by the [Cloud Datum Stream update API][update-api].
 3. Command line options
 2. Command line parameter JSON object, including `@@` file reference

For example, the following invocations produce equivalent results:

```sh
# using standard input
echo '{"enabled":false}' |s10k cloud-integrations datum-streams update --stream-id 100

# using parameter value
s10k cloud-integrations datum-streams update --stream-id 100 '{"enabled":false}'

# using parameter file reference - my-file.json contains {"enabled":false}
s10k cloud-integrations datum-streams update --stream-id 100 @@my-file.json

# using option
s10k cloud-integrations datum-streams update --stream-id 100 --disabled
```

The methods can be combined, with each method overriding settings duplicated in previous methods.
For example the following ends up changing the schedule to `300` because the command line parameter
override both the `--schedule` option and standard input values:

```sh
echo '{"schedule":900}' |s10k cloud-integrations datum-streams update --stream-id 100 \
    --schedule 600 '{"schedule":300}'
```

## Usage

```
s10k cloud-integrations datum-streams update
	[-rI]
	-stream=<datumStreamId>
	[-S=<serviceIdentifier>]
	[-m=<name>]
	[-source=<sourceId>]
	[-map=<mappingId>]
	[-w=<schedule>]
    [-prop=serviceProperty]...
	[-node=<nodeId> | -loc=<locationId>]
	[-e | -d]
    [-mode=<displayMode>]
	[<config>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-d`   | `--disabled` | make the entity disabled |
| `-e`   | `--enabled` | make the entity enabled |
| `-g=`  | `--merge-mode=` | one of `Simple`, `RecursiveObjects`, or `RecursiveObjectsAndArrays` to control the merge style; see [here][merge-option] for details |
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON settings object |
| `-loc=` | `--location-id=` | the location ID to set |
| `-m=`   | `--name=` | a name to set |
| `-map=` | `--mapping-id=` | the datum stream mapping ID to set |
| `-node=` | `--node-id=` | the node ID to set |
| `-prop=` | `--service-property` | a service property, in the form `path:value` or `@@file.json`; see [here][prop-option] for details |
| `-r` | `--replace` | replace the existing configuration completely, instead of merging in the changes provided |
| `-S=` | `--service=` | the service idenetifier to set; can be specified as a case-insensitive sub-string of a supported service, matched against both the service identifier and the display name, for example `also` will match the AlsoEnergy type |
| `-source=` | `--source-id=` | the source ID to set |
| `-stream=` | `--stream-id=` | the datum stream ID to update |
| `-mode=` | `--display-mode=` | the format to display the output as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to preview the update,
	without actually changing anything. For example:

	```sh
	s10k --dry-run cloud-integrations datum-streams update --stream-id 100 --disabled
	```

## Output

The updated datum stream (or a preview of the update if the `--dry-run` option was given).

## Examples

=== "Disable datum stream"

	```sh
	s10k cloud-integrations datum-streams update --stream-id 100 --disabled
	```

=== "Disable datum stream (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams`:

	```sh
	s10k c2c ds update --stream-id 100 --disabled
	```

=== "Pretty Output"

	```
	+-----+----------------+-----------+---------+------+-----------+---------------------+----------------+------------+--------------------------------------------------------+
	| ID  | Name           | Type      | Enabled | Kind | Object ID | Source ID           | Schedule       | Mapping ID | Service Properties                                     |
	+-----+----------------+-----------+---------+------+-----------+---------------------+----------------+------------+--------------------------------------------------------+
	| 100 | Big Solar Farm | SolarEdge | false   | n    |       123 | /BLD1/S1/R1/GEN/100 | 0 0/30 * * * * |        700 | {                                                      |
	|     |                |           |         |      |           | /BLD1/S1/R1/INV/1   |                |            |   "sourceIdMap" : {                                    |
	|     |                |           |         |      |           |                     |                |            |     "/0000000/met/Production" : "/BLD1/S1/R1/GEN/100", |
	|     |                |           |         |      |           |                     |                |            |     "/0000000/inv/77777777-4E" : "/BLD1/S1/R1/INV/1"   |
	|     |                |           |         |      |           |                     |                |            |   },                                                   |
	|     |                |           |         |      |           |                     |                |            |   "placeholders" : {                                   |
	|     |                |           |         |      |           |                     |                |            |     "siteId" : 0000000                                 |
	|     |                |           |         |      |           |                     |                |            |   }                                                    |
	|     |                |           |         |      |           |                     |                |            | }                                                      |
	+-----+----------------+-----------+---------+------+-----------+---------------------+----------------+------------+--------------------------------------------------------+
	```

=== "CSV Output"

	```csv
	ID,Name,Type,Enabled,Kind,Object ID,Source ID,Schedule,Mapping ID,Service Properties
	100,Big Solar Farm,SolarEdge,false,n,123,"/BLD1/S1/R1/GEN/100
	/BLD1/S1/R1/INV/1",0 0/30 * * * *,700,"{
	""sourceIdMap"" : {
		""/0000000/met/Production"" : ""/BLD1/S1/R1/GEN/100"",
		""/0000000/inv/77777777-4E"" : ""/BLD1/S1/R1/INV/1""
	},
	""placeholders"" : {
		""siteId"" : 0000000
	}
	}"
	```

=== "JSON Output"

	```json
	[
		{
			"configId": 100,
			"name": "Big Solar Farm",
			"serviceIdentifier": "s10k.c2c.ds.solaredge.v1",
			"created": "2025-02-26 11:21:31.409418Z",
			"modified": "2026-07-22 00:33:26.638189Z",
			"enabled": false,
			"datumStreamMappingId": 700,
			"schedule": "0 0/30 * * * *",
			"kind": "n",
			"objectId": 123,
			"sourceId": "unused",
			"serviceProperties": {
			"sourceIdMap": {
				"/0000000/met/Production": "/BLD1/S1/R1/GEN/100",
				"/0000000/inv/77777777-4E": "/BLD1/S1/R1/INV/1"
			},
				"placeholders": {
					"siteId": 0000000
				}
			}
		}
	]
	```

[datum-stream]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream
[merge-option]: ../../../service-properties.md#--merge-mode-option
[prop-option]: ../../../service-properties.md#--service-property-option
[update-api]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-update
