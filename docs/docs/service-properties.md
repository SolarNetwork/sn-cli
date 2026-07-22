# Service Properties

Some entities in the SolarNetwork API include a `serviceProperties` attribute that is an arbitrary
map data structure using string keys and arbitrary values. In JSON this is expressed as an object,
for example:

```json title="Example service properties"
{
	"apiKey": "{SSHA-256}aQrTEQH1rQoe...",
	"sourceIdMap": {
		"/123789/inv/98765": "/INV/1"
	},
	"virtualSourceIds": [
		"/GEN/1"
	]
}
```

!!! tip

	For more details, see [Service Properties][sprops] in the SolarNetwork wiki.

### `--merge-mode` option

Some commands support a `--merge-mode` option that controls how new service properties are merged
into pre-existing properties.

| Merge Mode | Description |
|:-----------|:------------|
| `Simple`   | Top-level keys are are **added** to the service properties, and will **replace** any existing value. |
| `RecursiveObjects` | Combine changes from **nested objects** within the service properties object. |
| `RecursiveObjectsAndArrays` | Combine changes from **nested objects and arrays** within the service properties object. |

!!! tip

	For more details, see [Merge operation][merge-op] in the SolarNetwork wiki.


### `--service-property` option

Some commands support a `--service-property` option that allows you to make changes to service
properties. It takes the form `path:value` where `path` is a `/`-delimited path in the metadata
document to update. For example imagine a [Cloud Datum Stream][cloud-datum-stream] entity with
these service properties:

```json
{
	"placeholders": {
		"siteId": 123456
	}
}
```

You could add another placeholder for `deviceId` like this:

```sh
s10k cloud-integrations datum-streams update-service-properties \
    --stream-id 100 \
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

#### Special service property encodings

The `value` given in a `--service-property path:value` option can be interpreted in some alternate
ways by changing the `:` delimiter to one of the following:

| Delimiter | Description |
|:----------|:------------|
| `{:`      | Treat as JSON. For example a `sourceIdMap` setting could be provided like `sourceIdMap{:{"/site123/dev234":"/BLD1/INV/1","/site123/dev567":"/BLD1/INV/2"}` |
| `%:`      | Treat as a comma-delimited list of equal-delimited key-value mappings. For example a `sourceIdMap` setting could be provided like `sourceIdMap%:/site123/dev23=/BLD1/INV/1,/site123/dev567=/BLD1/INV/2` |
| `[:`      | Treat as a comma-delimited list. For example a `virtualSourceIds` setting could be provided like `virtualSourceIds[:/BLD1/GEN/1,/BLD1/GEN/2` |
| `#:`      | Treat as a comma-delimited unique list. Duplicate values will be discarded. |

#### Service property file references

A `--service-property` value can be also be given as a JSON file reference in the form `@@file-path`.
For example, `--service-property @@settings.json`. The contents of the file must be a JSON object,
whose properties will be configured directly as service properties.

!!! warning

	The JSON properties loaded from the given file will be **added** to the existing
	service properties, **replacing** any existing key values.


[cloud-datum-stream]: https://github.com/SolarNetwork/solarnetwork/wiki/Cloud-Integrations#cloud-datum-stream-entity
[merge-op]: https://github.com/SolarNetwork/solarnetwork/wiki/Service-Properties#merge-operation
[sprops]: https://github.com/SolarNetwork/solarnetwork/wiki/Service-Properties
