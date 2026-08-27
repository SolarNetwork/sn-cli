---
title: create
---
# Cloud Datum Stream Create

Create a [Cloud Datum Stream][datum-stream] entity.

The configuration to make can be provided by a combination of methods:

 1. Standard input, as a JSON object in the form supported by the [Cloud Datum Stream craete API][create-api].
 3. Command line options
 2. Command line parameter JSON object, including `@@` file reference

For example, the following invocations produce equivalent results:

```sh
# using standard input
echo '{"name":"My Site","serviceIdentifier":"s10k.c2c.ds.solaredge.v2","kind":"n","objectId":10,"sourceId":"inv/1","datumStreamMappingId":8,"schedule":"900","enabled":true}' \
	|s10k cloud-integrations datum-streams create

# using parameter value
s10k cloud-integrations datum-streams create \
	'{"name":"My Site","serviceIdentifier":"s10k.c2c.ds.solaredge.v2","kind":"n","objectId":10,"sourceId":"inv/1","datumStreamMappingId":8,"schedule":"900","enabled":true}'

# using parameter file reference - my-file.json contains same JSON as above
s10k cloud-integrations datum-streams create @@my-file.json

# using options
s10k cloud-integrations datum-streams create --name "My Site" --service "solaredge.v2" \
	--node-id 10 --source-id "inv/1" --mapping-id 8 --schedule 900
```

The methods can be combined, with each method overriding settings duplicated in previous methods.
For example the following ends up setting the schedule to `300` because the command line parameter
overrides both the `--schedule` option and standard input values:

```sh
echo '{"name":"My Site","serviceIdentifier":"s10k.c2c.ds.solaredge.v2","kind":"n","objectId":10,"sourceId":"inv/1","datumStreamMappingId":8,"schedule":"900","enabled":true}' \
	|s10k cloud-integrations datum-streams create --schedule 600 \
	'{"schedule":300}'
```

## Usage

```
s10k cloud-integrations datum-streams create
	[-drI]
	[-g=<mode>]
	[-S=<serviceIdentifier>]
	[-m=<name>]
	[-source=<sourceId>]
	[-map=<mappingId>]
	[-w=<schedule>]
    [-prop=serviceProperty]...
	[-node=<nodeId> | -loc=<locationId>]
    [-mode=<displayMode>]
	[<config>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-d`   | `--disabled` | make the entity disabled |
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
| `-mode=` | `--display-mode=` | the format to display the output as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to preview the update,
	without actually changing anything. For example:

	```sh
	s10k --dry-run cloud-integrations datum-streams create ...
	```

## Output

The saved datum stream (or a preview of the update if the `--dry-run` option was given).

## Examples

=== "Create datum stream"

	```sh
	s10k cloud-integrations datum-streams create --name "My Site" --service "solaredge.v2" \
		--node-id 10 --source-id unused --mapping-id 8 --schedule 900 \
		--service-property 'sourceIdMap%:/111111/inv/7EAAAAAA-AA=inv/1,/111111/inv/7EBBBBBB-BB=inv/2'
	```

=== "Create datum stream (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams`:

	```sh
	s10k c2c ds create --name "My Site" --service "solaredge.v2" \
		--node-id 10 --source-id unused --mapping-id 8 --schedule 900 \
		--service-property 'sourceIdMap%:/111111/inv/7EAAAAAA-AA=inv/1,/111111/inv/7EBBBBBB-BB=inv/2'
	```

=== "Pretty Output"

	```
	╔════╤══════╤═══════════╤═════════╤══════╤═══════════╤═══════════╤══════════╤════════════╤════════════════════════════════════════════════════════════════════════════╗
	║ ID │ Name │ Type      │ Enabled │ Kind │ Object ID │ Source ID │ Schedule │ Mapping ID │ Service Properties                                                         ║
	╠════╪══════╪═══════════╪═════════╪══════╪═══════════╪═══════════╪══════════╪════════════╪════════════════════════════════════════════════════════════════════════════╣
	║ -1 │ Foo  │ SolarEdge │ true    │ n    │        10 │ inv/1     │ 900      │          8 │ sourceIdMap {/111111/inv/7EAAAAAA-AA=inv/1, /111111/inv/7EBBBBBB-BB=inv/2} ║
	║    │      │           │         │      │           │ inv/2     │          │            │                                                                            ║
	╚════╧══════╧═══════════╧═════════╧══════╧═══════════╧═══════════╧══════════╧════════════╧════════════════════════════════════════════════════════════════════════════╝
	```

=== "CSV Output"

	```csv
	ID,Name,Type,Enabled,Kind,Object ID,Source ID,Schedule,Mapping ID,Service Properties
	-1,Foo,SolarEdge,true,n,10,"inv/1
	inv/2",900,8,"sourceIdMap {/111111/inv/7EAAAAAA-AA=inv/1, /111111/inv/7EBBBBBB-BB=inv/2}
	"
	```

=== "JSON Output"

	```json
	{
	  "configId": -1,
	  "name": "Foo",
	  "serviceIdentifier": "s10k.c2c.ds.solaredge.v2",
	  "created": "2026-08-27 05:41:19.115753Z",
	  "modified": "2026-08-27 05:41:19.115753Z",
	  "enabled": true,
	  "datumStreamMappingId": 8,
	  "schedule": "900",
	  "kind": "n",
	  "objectId": 10,
	  "sourceId": "unused",
	  "serviceProperties": {
	    "sourceIdMap": {
	      "/111111/inv/7EAAAAAA-AA": "inv/1",
	      "/111111/inv/7EBBBBBB-BB": "inv/2"
	    }
	  }
	}
	```

[datum-stream]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream
[merge-option]: ../../../service-properties.md#-merge-mode-option
[prop-option]: ../../../service-properties.md#-service-property-option
[create-api]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-create
