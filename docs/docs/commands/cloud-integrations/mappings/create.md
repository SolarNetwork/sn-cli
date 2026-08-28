---
title: create
---
# Cloud Datum Stream Mapping Create

Create [Cloud Datum Stream Mapping][mapping] entities, along with associated [Cloud Datum Stream
Mapping Property][mapping-prop] entities.

The configuration to save can be provided by a combination of methods:

 1. Standard input, as a JSON object in the form supported by the [Cloud Datum Stream Mapping create
    API][create-api] with an additional `properties` JSON array in the form supported by the [Cloud
    Datum Stream Mapping Property create API][prop-create-api].
 3. Command line options
 2. Command line parameter JSON object, including `@@` file reference

For example, the following invocations produce equivalent results:

```sh
# using standard input
echo '{"name":"My Mapping","integrationId":1,"properties":[{"enabled":true,"propertyType":"a","propertyName":"wattHours","valueType":"r","valueReference":"/{siteId}/{hardwareId}/KWHnet/Last"}]}' \
    |s10k cloud-integrations mappings create

# using parameter value
s10k cloud-integrations mappings create \
    '{"name":"My Mapping","integrationId":1,"properties":[{"enabled":true,"propertyType":"a","propertyName":"wattHours","valueType":"r","valueReference":"/{siteId}/{hardwareId}/KWHnet/Last"}]}'

# using parameter file reference - my-file.json contains same JSON as above
s10k cloud-integrations mappings create @@my-file.json

# using options
s10k cloud-integrations mappings create --name 'My Mapping' --integration-id 1 \
	--property 'i,watts,r,/{siteId}/inv/*/W' \
	--property 'a,wattHours,r,/{siteId}/inv/*/TotWhExp'
```

The methods can be combined, with each method overriding settings duplicated in previous methods.
For example the following ends up changing the name to `Mapping C` because the command line parameter
overrides both the `--name` option and standard input values:

```sh
echo '{"name":"Mapping A"}' |s10k cloud-integrations mappings create \
	--name 'Mapping B' \
	--integration-id 1 \
	--property 'i,watts,r,/{siteId}/inv/*/W' \
    '{"name":"Mapping C"}'
```

## Usage

```
s10k cloud-integrations mappings create
	[-I]
	[-i=<integrationId>]
	[-m=<name>]
	[-prop=serviceProperty]...
	[-p=propertyDefinition]...
	[-g=<mode>]
    [-mode=<displayMode>]
	[<config>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-g=`  | `--merge-mode=` | one of `Simple`, `RecursiveObjects`, or `RecursiveObjectsAndArrays` to control the merge style; see [here][merge-option] for details |
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON configuration object |
| `-i=`   | `--integration-id=` | the integration ID(s) to set |
| `-m=`     | `--name=` | the display name to set |
| `-p=` | `--property=` | a mapping property, in the form `[index,]type,name,val_type,ref[,multiplier][,scale]`; see [Property Definitions](#property-definitions) for more details |
| `-prop=` | `--service-property` | a service property, in the form `path:value` or `@@file.json`; see [here][prop-option] for details |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to simulate what
	would be created, without actually saving anything. For example:

	```sh
	s10k --dry-run cloud-integrations mappings create --name 'My Mapping' --integration-id 1 \
		--property 'i,watts,r,/{siteId}/inv/*/W'
	```

### Property Definitions

The `--property` option accepts a _property definition_ value in this form:

``` title="Property Definition syntax"
[index,]type,name,val_type,ref[,multiplier][,scale]
```

| Component    | Description |
|:-------------|:------------|
| `index`      | An optional property ordering integer value. Smaller values sort before larger ones. Omit to assign the property index automatically based on the order of the `--property` options. |
| `type`       | The [datum property classification][datum-samples-type], one of `i`, `a`, or `s`. |
| `name`       | The datum property name. |
| `val_type`   | Either `r` for a normal reference or `s` for a Spel Expression. |
| `ref`        | The value reference, or expression if the `val_type` is `s`. |
| `multiplier` | An optional decimal number to multiply captured data values by. |
| `scale`      | An optional maximum number of decimal places to round data values to. |

## Output

The created mapping and associated properties.

## Examples

=== "Create mapping"

	```sh
	s10k cloud-integrations mappings create --name 'My Mapping' --integration-id 7 \
		--property 'i,watts,r,/{siteId}/inv/*/W' \
		--property 'a,wattHours,r,/{siteId}/inv/*/TotWhExp'
	```

=== "Create mapping (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `maps` instead of `mappings`:

	```sh
	s10k c2c maps create --name 'My Mapping' --integration-id 7 \
		--property 'i,watts,r,/{siteId}/inv/*/W' \
		--property 'a,wattHours,r,/{siteId}/inv/*/TotWhExp'
	```

=== "Pretty Output"

	```
	╔════╤════════════╤════════════════╤═════════════════════╤═════════════════════╤════════════╤══════════════════╤═══════════════╤═══════════════╤════════════╤══════════════════════════╤════════════╤═══════╗
	║ ID │ Name       │ Integration ID │ Integration Name    │ Integration Enabled │ Property # │ Property Enabled │ Property Type │ Property Name │ Value Type │ Value Reference          │ Multiplier │ Scale ║
	╠════╪════════════╪════════════════╪═════════════════════╪═════════════════════╪════════════╪══════════════════╪═══════════════╪═══════════════╪════════════╪══════════════════════════╪════════════╪═══════╣
	║ 10 │ My Mapping │              7 │ SolarEdge - BigFarm │ true                │          0 │ true             │ Instantaneous │ watts         │ Reference  │ /{siteId}/inv/*/W        │            │       ║
	╟────┼────────────┼────────────────┼─────────────────────┼─────────────────────┼────────────┼──────────────────┼───────────────┼───────────────┼────────────┼──────────────────────────┼────────────┼───────╢
	║    │            │                │                     │                     │          1 │ true             │ Accumulating  │ wattHours     │ Reference  │ /{siteId}/inv/*/TotWhExp │            │       ║
	╚════╧════════════╧════════════════╧═════════════════════╧═════════════════════╧════════════╧══════════════════╧═══════════════╧═══════════════╧════════════╧══════════════════════════╧════════════╧═══════╝
	```

=== "CSV Output"

	```csv
	ID,Name,Integration ID,Integration Name,Integration Enabled,Property #,Property Enabled,Property Type,Property Name,Value Type,Value Reference,Multiplier,Scale
	-1,My Mapping,7,SolarEdge - BigFarm,true,0,true,Instantaneous,watts,Reference,/{siteId}/inv/*/W,,
	,,,,,1,true,Accumulating,wattHours,Reference,/{siteId}/inv/*/TotWhExp,,
	```

=== "JSON Output"

	```json
	{
	  "configId": 10,
	  "name": "My Mapping",
	  "created": "2026-08-28 06:15:06.822086Z",
	  "modified": "2026-08-28 06:15:06.822086Z",
	  "integrationId": 7,
	  "integration": {
	    "configId": 7,
	    "name": "SolarEdge - BigFarm",
	    "serviceIdentifier": "s10k.c2c.i9n.solaredge.v1",
	    "created": "2026-04-30 04:40:54.272321Z",
	    "modified": "2026-04-30 04:40:54.272321Z",
	    "enabled": true,
	    "serviceProperties": {
	      "apiKey": "{SSHA-256}t2cQOmKGclw2EDgDmX0Um/fKO42BYtdh7vuUqVIQAcZ+S5AJEKtElA=="
	    }
	  },
	  "properties": [
	    {
	      "datumStreamMappingId": 10,
	      "index": 0,
	      "created": "2026-08-28 06:15:06.822086Z",
	      "modified": "2026-08-28 06:15:06.822086Z",
	      "enabled": true,
	      "propertyType": "i",
	      "propertyName": "watts",
	      "valueType": "r",
	      "valueReference": "/{siteId}/inv/*/W"
	    },
	    {
	      "datumStreamMappingId": -1,
	      "index": 1,
	      "created": "2026-08-28 06:15:06.822086Z",
	      "modified": "2026-08-28 06:15:06.822086Z",
	      "enabled": true,
	      "propertyType": "a",
	      "propertyName": "wattHours",
	      "valueType": "r",
	      "valueReference": "/{siteId}/inv/*/TotWhExp"
	    }
	  ]
	}
	```


[create-api]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-mapping-property-create
[datum-samples-type]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#datum-property-classifications
[mapping]: https://github.com/SolarNetwork/solarnetwork/wiki/Cloud-Integrations#cloud-datum-stream-mapping-entity
[mapping-prop]: https://github.com/SolarNetwork/solarnetwork/wiki/Cloud-Integrations#cloud-datum-stream-mapping-property-entity
[merge-option]: ../../../service-properties.md#-merge-mode-option
[prop-option]: ../../../service-properties.md#-service-property-option
[prop-create-api]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-mapping-property-create
[integration]: https://github.com/SolarNetwork/solarnetwork/wiki/Cloud-Integrations#cloud-integration-entity

