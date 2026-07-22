---
title: update-service-properties
---
# Cloud Integration Update Service Properties

Make changes to an existing [Cloud Integration][integration] entity's [service properties][sprops].

The changes to make can be provided by a combination of methods:

 1. Standard input, as a JSON object.
 3. Command line options
 2. Command line parameter JSON object, or `@@` file reference

For example, the following invocations produce equivalent results:

```sh
# using standard input
echo '{"alternateName":"Big Site"}' \
    |s10k cloud-integrations integrations update-service-properties \
	    --integration-id 100

# using parameter value
s10k cloud-integrations integrations update-service-properties \
    --integration-id 100 '{"alternateName":"Big Site"}'

# using parameter file reference - my-file.json contains '{"alternateName":"Big Site"}'
s10k cloud-integrations integrations update-service-properties \
    --integration-id 100 @@my-file.json

# using option
s10k cloud-integrations integrations update-service-properties \
    --integration-id 100 --service-property 'alternateName:Big Site'
```

The methods can be combined, with each method overriding settings duplicated in previous methods.
For example the following ends up changing `alternateName` to `Small Site` because the command line parameter
override both the `--service-property` option and standard input values:

```sh
echo '{"alternateName":"Big Site"}' \
    |s10k cloud-integrations integrations update-service-properties \
        --integration-id 100 \
        --service-property 'alternateName:Medium Site' \
	    '{"alternateName":"Small Site"}'
```

## Usage

```
s10k cloud-integrations integrations update-service-properties
	[-I]
	-i=<integrationId>
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
| `-i=` | `--integration-id=` | the integration ID to update |
| `-mode=` | `--display-mode=` | the format to display the output as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to preview the update,
	without actually changing anything. For example:

	```sh
	s10k --dry-run cloud-integrations integrations update-service-properties \
	     --integration-id 100 --service-property 'alternateName:My Site'
	```

## Output

The updated integration service properties (or a preview of the update if the `--dry-run` option was given).

## Examples

=== "Add username"

	```sh
	s10k cloud-integrations integrations update-service-properties \
	    --integration-id 100 \
		--service-property 'username:theusername'
	```

=== "Add username (shortcut)"

	You can use `c2c` instead of `cloud-integrations` and `i9n` instead of `integrations` and
	`update-props` instead of `update-service-properties`, for example:

	```sh
	s10k c2c i9n update-props --integration-id 100 --service-property 'username:theusername'
	```

=== "Pretty Output"

	```
	+-------------------+------------------------------------------+
	| Property          | Value                                    |
	+-------------------+------------------------------------------+
	| username          | theusername                              |
	+-------------------+------------------------------------------+
	| password          | {SSHA-256}7PHQ1aQIT+oQ/X1k7m6RPGuz...    |
	+-------------------+------------------------------------------+
	```

=== "CSV Output"

	```csv
	Property,Value
	username,theusername
	password,{SSHA-256}7PHQ1aQIT+oQ/X1k7m6RPGuz...
	```

=== "JSON Output"

	```json
	{
		"username": "theuesrname",
		"password": "{SSHA-256}7PHQ1aQIT+oQ/X1k7m6RPGuz..."
	}
	```


[integration]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Cloud-Integrations-API#cloud-integration
[merge-option]: ../../../service-properties.md#--merge-mode-option
[prop-option]: ../../../service-properties.md#--service-property-option
[sprops]: ../../../service-properties.md
