---
title: create
---
# Location Request Create

Create location requests.

The request to save can be provided by a combination of methods:

 1. Standard input, as a JSON object in the form supported by the [Location Request add API][create-api].
 3. Command line options
 2. Command line parameter JSON object, including `@@` file reference

For example, the following invocations produce equivalent results:

```sh
# using standard input
echo '{"sourceId":"OpenWeatherMap", "features":["day","forecast","weather"], "location":{"name":"Wellington","region":"Wellington","locality":"Wellington","zone":"Pacific/Auckland","country":"NZ"}}' \
    |s10k locations requests create

# using parameter value
s10k locations requests create \
    '{"sourceId":"OpenWeatherMap", "features":["day","forecast","weather"], "location":{"name":"Wellington","region":"Wellington","locality":"Wellington","zone":"Pacific/Auckland","country":"NZ"}}'

# using parameter file reference - my-file.json contains same JSON as above
s10k locations requests create @@my-file.json

# using options
s10k locations requests create --source-id OpenWeatherMap --feature weather,day,forecast \
	--name Wellington --region Wellington --locality Wellington --country NZ \
	--time-zone Pacific/Auckland
```

The methods can be combined, with each method overriding settings duplicated in previous methods.
For example the following ends up changing the name to `Welly C` because the command line parameter
overrides both the `--name` option and standard input values:

```sh
echo '{"name":"Welly A"}' |s10k locations requests create \
	--source-id OpenWeatherMap --feature weather,day,forecast \
	--name "Welly B" --region Wellington --locality Wellington \
	--country NZ --time-zone Pacific/Auckland \
    '{"name":"Welly C"}'
```

## Usage

```
s10k locations requests create
	[-I]
	[-source=<sourceId>]
	[-f=feature[,feature...]]...
	[-m=<name>]
    [-l=<locality>]
	[-r=<region>]
    [-s=<stateOrProvince>]
	[-p=<postalCode>]
    [-c=<country>]
	[-tz=<zone>]
    [-mode=<displayMode>]
	[<config>]
```

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-c=` | `--country=` | a country code, like `NZ` or `US` |
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON configuration object |
| `-l=`     | `--locality=` • `--city` | the locality |
| `-m=`     | `--name=` | the display name |
| `-p=` | `--postal-code=` • `--zip-code` | a postal code |
| `-r=` | `--region=` | a region name |
| `-s=` | `--state` • `--province` | a state or province name |
| `-source=` | `--source-id` | a location source ID |
| `-tz=` | `--time-zone=` | a time zone ID, like `Pacific/Auckland` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to simulate what
	would be created, without actually saving anything. For example:

	```sh
	s10k --dry-run locations requests create --source-id OpenWeatherMap \
	--feature weather,day,forecast --name Wellington --region Wellington \
	--locality Wellington --country NZ --time-zone Pacific/Auckland
	```

## Output

The created request, or preview if the `--dry-run` global option given.

## Examples

=== "Create request"

	```sh
	s10k locations requests create --source-id OpenWeatherMap \
		--feature weather,day,forecast --name Wellington --region Wellington \
		--locality Wellington --country NZ --time-zone Pacific/Auckland
	```

=== "Create request (shortcut)"

	You can use `locs` instead of `locations` and `reqs` instead of `requests`:

	```sh
	s10k locs reqs create --source-id OpenWeatherMap \
		--feature weather,day,forecast --name Wellington --region Wellington \
		--locality Wellington --country NZ --time-zone Pacific/Auckland
	```

=== "Pretty Output"

	```
	╔════╤═════════════════════════════════╤═════════════════════════════════╤═══════════╤═════════════╤════════════════╤════════════════════════╤═══════════════════════════╤═════════╗
	║ ID │ Created                         │ Modified                        │ Status    │ Location ID │ Source ID      │ Features               │ Info                      │ Message ║
	╠════╪═════════════════════════════════╪═════════════════════════════════╪═══════════╪═════════════╪════════════════╪════════════════════════╪═══════════════════════════╪═════════╣
	║  3 │ 2026-08-30 17:42:37.16777+12:00 │ 2026-08-30 17:42:37.16777+12:00 │ Submitted │             │ OpenWeatherMap │ weather, forecast, day │ name     Wellington       │         ║
	║    │                                 │                                 │           │             │                │                        │ zone     Pacific/Auckland │         ║
	║    │                                 │                                 │           │             │                │                        │ region   Wellington       │         ║
	║    │                                 │                                 │           │             │                │                        │ country  NZ               │         ║
	║    │                                 │                                 │           │             │                │                        │ locality Wellington       │         ║
	║    │                                 │                                 │           │             │                │                        │                           │         ║
	╚════╧═════════════════════════════════╧═════════════════════════════════╧═══════════╧═════════════╧════════════════╧════════════════════════╧═══════════════════════════╧═════════╝
	```

=== "CSV Output"

	```csv
	ID,Created,Modified,Status,Location ID,Source ID,Features,Info,Message
	3,2026-08-30 17:42:57.51229+12:00,2026-08-30 17:42:57.51229+12:00,Submitted,,OpenWeatherMap,"weather, forecast, day","name     Wellington
	zone     Pacific/Auckland
	region   Wellington
	country  NZ
	locality Wellington
	",
	```

=== "JSON Output"

	```json
	{
	  "id": 3,
	  "created": "2026-08-30 05:30:10.387888Z",
	  "modified": "2026-08-30 05:30:10.387888Z",
	  "userId": 147,
	  "status": "Submitted",
	  "data": {
	    "features": [
	      "weather",
	      "forecast",
	      "day"
	    ],
	    "location": {
	      "name": "Wellington",
	      "zone": "Pacific/Auckland",
	      "region": "Wellington",
	      "country": "NZ",
	      "locality": "Wellington"
	    },
	    "sourceId": "OpenWeatherMap"
	  }
	}
	```


[create-api]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Location-Request-API#location-request-add

