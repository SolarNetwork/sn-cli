---
title: update
---
# Cloud Datum Stream Update

Make changes to an existing [Cloud Datum Stream][datum-stream] entity.

The changes to make can be provided by a combination of methods:

 1. Standard input, as a JSON object in the form supported by the [Cloud Datum Stream update API][update-api].
 3. Command line options
 2. Command line parameter, including `@@` file reference

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
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-d`   | `--disabled` | make the entity disabled |
| `-e`   | `--enabled` | make the entity enabled |
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON settings object |
| `-loc=` | `--location-id=` | the location ID to set |
| `-m=`   | `--name=` | a name to set |
| `-map=` | `--mapping-id=` | the datum stream mapping ID to set |
| `-node=` | `--node-id=` | the node ID to set |
| `-prop=` | `--service-property` | a service property, in the form `path:value`; see [Service property option](#--service-property-option) for details |
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

### Replace vs merge

By default the setting updates given as input to this command are _merged_ into any existing settings
on the datum stream entity. Thus you only need to provide values for settings you need to change, and
all other settings will remain the same.

You can instead _replace_ all settings with new values by including the `--replace` option. In this
mode you must provide the entire configuration required by a cloud datum stream, and any optional
settings you do not provide will end up empty in the updated entity. This mode can be handy if you
are generating a full configuration JSON document outside of this tool, and then provide that as
standard input to this command.

### `--service-property` option

The `--service-property` option allows you to make changes to the service properties metadata. It
takes the form `path:value` where `path` is a `/`-delimited path in the metadata document to update.
For example imagine a datum stream with these service properties:

```json
{
	"placeholders": {
		"siteId": 123456
	}
}
```

You could add another placeholder for `deviceId` like this:

```sh
s10k cloud-integrations datum-streams update --stream-id 100 \
    --service-property placeholders/deviceId=ABCDEF
```

That would result in service properties like this:

```json
{
	"placeholders": {
		"siteId": 123456,
		"deviceId": "ABCDEF"
	}
}
```

Multiple `--service-property` options are allowed, and they are processed in the order given on
the command line.

#### Special service property values

The `value` given in a `--service-property path:value` option can be interpreted in some alternate
ways by changing the `:` delimiter to one of the following:

| Delimiter | Description |
|:----------|:------------|
| `{:`      | Treat as JSON. For example a `sourceIdMap` setting could be provided like `sourceIdMap{:{"/site123/dev234":"/BLD1/INV/1","/site123/dev567":"/BLD1/INV/2"}` |
| `%:`      | Treat as a comma-delimited list of equal-delimited key-value mappings. For example a `sourceIdMap` setting could be provided like `sourceIdMap%:/site123/dev23=/BLD1/INV/1,/site123/dev567=/BLD1/INV/2` |
| `[:`      | Treat as a comma-delimited list. For example a `virtualSourceIds` setting could be provided like `virtualSourceIds[:/BLD1/GEN/1,/BLD1/GEN/2` |
| `#:`      | Treat as a comma-delimited unique list. Duplicate values will be discarded. |

## Output

The updated datum stream.

[datum-stream]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream
[update-api]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-datum-stream-update
