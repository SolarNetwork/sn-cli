---
title: update
---
# Cloud Integration Update

Update [Cloud Integration][integration] entities.

The configuration to save can be provided by a combination of methods:

 1. Standard input, as a JSON object in the form supported by the [Cloud Integration update API][update-api].
 3. Command line options
 2. Command line parameter JSON object, including `@@` file reference

For example, the following invocations produce equivalent results:

```sh
# using standard input
echo '{"serviceProperties":{"apiKey":"abc123"}}' \
    |s10k cloud-integrations integrations update --integration-id 10

# using parameter value
s10k cloud-integrations integrations update --integration-id 10 \
    '{"serviceProperties":{"apiKey":"abc123"}}'

# using parameter file reference - my-file.json contains same JSON as above
s10k cloud-integrations integrations update --integration-id 10 @@my-file.json

# using options
s10k cloud-integrations integrations update --integration-id 10 \
	--service-property 'aipKey:abc123'
```

The methods can be combined, with each method overriding settings duplicated in previous methods.
For example the following ends up changing the name to `SolarEdge C` because the command line parameter
overrides both the `--name` option and standard input values:

```sh
echo '{"name":"SolarEdge A"}' |s10k cloud-integrations integrations update --integration-id 10 \
	--name 'SolarEdge B' \
    '{"name":"SolarEdge C"}'
```

## Usage

```
s10k cloud-integrations integrations update
	[-dI]
	-i=<integrationId>
	[-m=<name>]
    [-S=<serviceIdentifier>]
	[-prop=serviceProperty]...
    [-mode=<displayMode>]
	[<config>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-d` | `--disabled` | update in disabled state |
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON configuration object |
| `-i=` | `--integration-id=` | the ID of the integration to update |
| `-m=`     | `--name=` | the display name to set |
| `-prop=` | `--service-property` | a service property, in the form `path:value` or `@@file.json`; see [here][prop-option] for details |
| `-S=` | `--service=` | the Cloud Integration service idenetifier to set; can be specified as a case-insensitive sub-string of a supported service, matched against both the service identifier and the display name, for example `also` will match the AlsoEnergy type |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to simulate what
	would be updated, without actually saving anything. For example:

	```sh
	s10k --dry-run cloud-integrations integrations update --integration-id 10 \
		--name "SolarEdge"
		--service-property 'apiKey:abcdef123456789'
	```

## Output

The created integration.

## Examples

=== "Update integration"

	```sh
	s10k cloud-integrations integrations update  --integration-id 10 --name "NE Project"
	```

=== "Update integration (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `i9n` instead of `integrations`:

	```sh
	s10k c2c i9n update  --integration-id 10 --name "NE Project"
	```

=== "Pretty Output"

	```
	╔════╤════════════╤═══════════╤═════════╤═══════════════════════════════════════════════════════════════════════════╗
	║ ID │ Name       │ Type      │ Enabled │ Service Properties                                                        ║
	╠════╪════════════╪═══════════╪═════════╪═══════════════════════════════════════════════════════════════════════════╣
	║ 16 │ NE Project │ SolarEdge │ true    │ apiKey {SSHA-256}u0ItHLfZsEIlJzpa2GKSmOtbfAapE2QwRmAZ5SWkId10+5IhEV3Uvg== ║
	║    │            │           │         │                                                                           ║
	╚════╧════════════╧═══════════╧═════════╧═══════════════════════════════════════════════════════════════════════════╝
	```

=== "CSV Output"

	```csv
	ID,Name,Type,Enabled,Service Properties
	10,NE Project,SolarEdge,true,"apiKey {SSHA-256}u0ItHLfZsEIlJzpa2GKSmOtbfAapE2QwRmAZ5SWkId10+5IhEV3Uvg==
	"
	```

=== "JSON Output"

	```json
	{
	  "configId": 10,
	  "name": "NE Project",
	  "serviceIdentifier": "s10k.c2c.i9n.solaredge.v2",
	  "created": "2026-08-26 07:14:55.464542Z",
	  "modified": "2026-08-27 08:14:55Z",
	  "enabled": true,
	  "serviceProperties": {
	    "apiKey": "{SSHA-256}u0ItHLfZsEIlJzpa2GKSmOtbfAapE2QwRmAZ5SWkId10+5IhEV3Uvg=="
	  }
	}
	```


[update-api]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-integration-update
[integration]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-integration
[prop-option]: ../../../service-properties.md#-service-property-option

