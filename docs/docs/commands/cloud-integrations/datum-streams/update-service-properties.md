---
title: update-service-properties
---
# Cloud Datum Stream Update Service Properties

Make changes to an existing [Cloud Datum Stream][datum-stream] entity's [service properties][sprops].

The changes to make can be provided by a combination of methods:

 1. Standard input, as a JSON object.
 3. Command line options
 2. Command line parameter JSON object, or `@@` file reference

For example, the following invocations produce equivalent results:

```sh
# using standard input
echo '{"alternateName":"Big Site"}' \
    |s10k cloud-integrations datum-streams update-service-properties \
	    --stream-id 100

# using parameter value
s10k cloud-integrations datum-streams update-service-properties \
    --stream-id 100 '{"alternateName":"Big Site"}'

# using parameter file reference - my-file.json contains '{"alternateName":"Big Site"}'
s10k cloud-integrations datum-streams update-service-properties \
    --stream-id 100 @@my-file.json

# using option
s10k cloud-integrations datum-streams update-service-properties \
    --stream-id 100 --service-property 'alternateName:Big Site'
```

The methods can be combined, with each method overriding settings duplicated in previous methods.
For example the following ends up changing `alternateName` to `Small Site` because the command line
parameter overrides both the `--service-property` option and standard input values:

```sh
echo '{"alternateName":"Big Site"}' \
    |s10k cloud-integrations datum-streams update-service-properties \
        --stream-id 100 \
        --service-property 'alternateName:Medium Site' \
	    '{"alternateName":"Small Site"}'
```

## Usage

```
s10k cloud-integrations datum-streams update-service-properties
	[-I]
	-stream=<datumStreamId>
	[-g=<mode>]
    [-prop=serviceProperty]...
    [-mode=<displayMode>]
	[<config>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-g=`  | `--merge-mode=` | one of `Simple`, `RecursiveObjects`, or `RecursiveObjectsAndArrays` to control the merge style; see [here][merge-option] for details |
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON settings object |
| `-prop=` | `--service-property` | a service property, in the form `path:value` or `@@file.json`; see [here][prop-option] for details |
| `-stream=` | `--stream-id=` | the datum stream ID to update |
| `-mode=` | `--display-mode=` | the format to display the output as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to preview the update,
	without actually changing anything. For example:

	```sh
	s10k --dry-run cloud-integrations datum-streams update-service-properties \
	     --stream-id 100 --service-property 'alternateName:My Site'
	```

## Output

The updated datum stream service properties (or a preview of the update if the `--dry-run` option was given).

## Examples

=== "Add placeholder"

	```sh
	s10k cloud-integrations datum-streams update-service-properties \
	    --stream-id 100 \
		--service-property 'placeholders/deviceId:1111111'
	```

=== "Add placeholder (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `ds` instead of `datum-streams` and
	`update-props` instead of `update-service-properties`, for example:

	```sh
	s10k c2c ds update-props --stream-id 100 --service-property 'placeholders/deviceId:1111111'
	```

=== "Pretty Output"

	```
	+-------------------+--------------------------------------------------------------------------------+
	| Property          | Value                                                                          |
	+-------------------+--------------------------------------------------------------------------------+
	| sourceIdMap       | /0000000/met/Production=/BLD1/S1/R1/GEN/100,/0000000/inv/77777777-4E=/BLD1/S1/ |
	|                   | R1/INV/1                                                                       |
	+-------------------+--------------------------------------------------------------------------------+
	| placeholders      | siteId=0000000,deviceId=1111111                                                |
	+-------------------+--------------------------------------------------------------------------------+
	```

=== "CSV Output"

	```csv
	Property,Value
	sourceIdMap,"/0000000/met/Production=/BLD1/S1/R1/GEN/100,/0000000/inv/77777777-4E=/BLD1/S1/R1/INV/1"
	placeholders,siteId=0000000
	serviceProperties,placeholders={deviceId=1111111}
	```

=== "JSON Output"

	```json
	{
		"sourceIdMap": {
			"/0000000/met/Production": "/BLD1/S1/R1/GEN/100",
			"/0000000/inv/77777777-4E": "/BLD1/S1/R1/INV/1"
		},
		"placeholders": {
			"siteId": 0000000,
			"deviceId": "1111111"
		}
	}
	```


[datum-stream]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream
[merge-option]: ../../../service-properties.md#-merge-mode-option
[prop-option]: ../../../service-properties.md#-service-property-option
[sprops]: ../../../service-properties.md
