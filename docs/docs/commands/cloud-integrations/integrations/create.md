---
title: create
---
# Cloud Integration Create

Create [Cloud Integration][integration] entities.

The settings to save can be provided by a combination of methods:

 1. Standard input, as a JSON object in the form supported by the [Cloud Integration create API][create-api].
 3. Command line options
 2. Command line parameter JSON object, including `@@` file reference

For example, the following invocations produce equivalent results:

```sh
# using standard input
echo '{"name":"SolarEdge", "serviceIdentifier":"s10k.c2c.i9n.solaredge.v2", "serviceProperties":{"apiKey":"abc123"}}' \
    |s10k cloud-integrations integrations create

# using parameter value
s10k cloud-integrations integrations create \
    '{"name":"SolarEdge", "serviceIdentifier":"s10k.c2c.i9n.solaredge.v2", "serviceProperties":{"apiKey":"abc123"}}'

# using parameter file reference - my-file.json contains same JSON as above
s10k cloud-integrations integrations create @@my-file.json

# using options
s10k cloud-integrations integrations create --name SolarEdge --service solaredge.v2 \
	--service-property 'aipKey:abc123'
```

The methods can be combined, with each method overriding settings duplicated in previous methods.
For example the following ends up changing the name to `SolarEdge C` because the command line parameter
overrides both the `--name` option and standard input values:

```sh
echo '{"name":"SolarEdge A"}' |s10k cloud-integrations integrations create --name 'SolarEdge B' \
	--service solaredge.v2 --service-property 'aipKey:abc123' \
    '{"name":"SolarEdge C"}'
```

## Usage

```
s10k cloud-integrations integrations create
	[-dI]
	-m=<name>
    -S=<serviceIdentifier>
	[-prop=serviceProperty]...
    [-mode=<displayMode>]
	[<service properties>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-d` | `--disabled` | create in disabled state |
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON settings object |
| `-m=`     | `--name=` | the display name to set |
| `-prop=` | `--service-property` | a service property, in the form `path:value` or `@@file.json`; see [here][prop-option] for details |
| `-S=` | `--service=` | the Cloud Integration service idenetifier to set; can be specified as a case-insensitive sub-string of a supported service, matched against both the service identifier and the display name, for example `also` will match the AlsoEnergy type |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to simulate what
	would be created, without actually saving anything. For example:

	```sh
	s10k --dry-run cloud-integrations integrations create --name "SolarEdge" --service solaredge.v2
		--service-property 'apiKey:abcdef123456789'
	```

## Output

The created integration.

## Examples

=== "Create integration"

	```sh
	s10k cloud-integrations integrations create --name "SolarEdge" --service solaredge.v2
		--service-property 'apiKey:abcdef123456789'
	```

=== "Create integration (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `i9n` instead of `integrations`:

	```sh
	s10k c2c i9n create --name "SolarEdge" --service solaredge.v2
		--service-property 'apiKey:abcdef123456789'
	```

=== "Pretty Output"

	```
	╔════╤═══════════╤═══════════╤═════════╤═══════════════════════════════════════════════════════════════════════════╗
	║ ID │ Name      │ Type      │ Enabled │ Service Properties                                                        ║
	╠════╪═══════════╪═══════════╪═════════╪═══════════════════════════════════════════════════════════════════════════╣
	║ 10 │ SolarEdge │ SolarEdge │ true    │ apiKey {SSHA-256}x8E12TnJ4PjzHOCcOZJEOxkrRWvHj1eXokVnOUPkOxL12UZ4l4UBug== ║
	║    │           │           │         │                                                                           ║
	╚════╧═══════════╧═══════════╧═════════╧═══════════════════════════════════════════════════════════════════════════╝
	```

=== "CSV Output"

	```csv
	ID,Name,Type,Enabled,Service Properties
	10,SolarEdge,SolarEdge,true,"apiKey {SSHA-256}x8E12TnJ4PjzHOCcOZJEOxkrRWvHj1eXokVnOUPkOxL12UZ4l4UBug==
	"
	```

=== "JSON Output"

	```json
	{
	  "configId": 10,
	  "name": "SolarEdge",
	  "serviceIdentifier": "s10k.c2c.i9n.solaredge.v2",
	  "created": "2026-08-26 07:14:55.464542Z",
	  "modified": "2026-08-26 07:14:55.464542Z",
	  "enabled": true,
	  "serviceProperties": {
	    "apiKey": "{SSHA-256}x8E12TnJ4PjzHOCcOZJEOxkrRWvHj1eXokVnOUPkOxL12UZ4l4UBug=="
	  }
	}
	```


[create-api]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-integration-create
[integration]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-integration
[prop-option]: ../../../service-properties.md#-service-property-option

