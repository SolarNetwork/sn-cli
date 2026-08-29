---
title: list
---
# Locations List

List locations matching search criteria. Multiple criteria options are combined with a logical _"and"_, meaning
all criteria must match a location to be included in the results.

## Usage

```
s10k locations list
	[-m=<name>]
	[-r=<region>]
	[-s=<stateOrProvince>]
    [-p=<postalCode>]
	[-c=<country>]
    [-tz=<zone>]
	[-sort=orderKey[,orderKey...]]...
	[-M=max] [-O=<resultOffset>]
	[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-c=` | `--country=` | a country code to match, like `NZ` or `US` |
| `-M=` | `--max=` | the maximum number of records to return |
| `-m=` | `--name=` | a case-insensitive keyword search term, matching country, region, state, and locality names |
| `-node=` | `--node-id=` | the node ID(s) to match |
| `-O=` | `--offset=` | start returning records from this offset, `0` being the first record |
| `-p=` | `--postal-code=` • `--zip-code` | a postal code to match |
| `-r=` | `--region=` | a region name to match |
| `-s=` | `--state` • `--province` | a state or province name to match |
| `-sort=` | `--sort-by=` | the sort key(s) to order the results by, one of `Source`, `Name`, `Country`, `Region`, `StateOrProvince`, `PostalCode`, `TimeZoneId`; note that `TimeZoneId` orders the results based on the time zone ID name, not its offset from UTC |
| `-tz=` | `--time-zone=` | a time zone ID to match, like `Pacific/Auckland` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of matching locations. Each record contains the following properties:

| Property | Description |
|:---------|:------------|
| **ID** | A unique identifier for the location. |
| **Country** | The country code, for example `NZ` or `US`. |
| **Region** | The region name. |
| **Time Zone** | The time zone ID, for example `Pacific/Auckland` or `UTC`. |
| **Locatlity** | The locality (city) name. |
| **Postal Code** | The postal code. |

## Examples

=== "List locations"

	```sh
	s10k locations list --name wellington
	```

=== "List locations (shortcut)"

	You can use `locs` instead of `locations`:

	```sh
	s10k locs list --name wellington
	```

=== "Pretty Output"

	```
	╔══════════╤═════════╤════════════╤════════════════╤══════════════════╤═════════════════╤═════════════╗
	║ ID       │ Country │ Region     │ State/Province │ Time Zone        │ Locality        │ Postal Code ║
	╠══════════╪═════════╪════════════╪════════════════╪══════════════════╪═════════════════╪═════════════╣
	║ 11565134 │ NZ      │            │ Wellington     │ Pacific/Auckland │ Wellington City │             ║
	╟──────────┼─────────┼────────────┼────────────────┼──────────────────┼─────────────────┼─────────────╢
	║ 11536889 │ NZ      │ Wellington │                │ Pacific/Auckland │ Featherston     │             ║
	╟──────────┼─────────┼────────────┼────────────────┼──────────────────┼─────────────────┼─────────────╢
	║ 11536821 │ NZ      │ Wellington │                │ Pacific/Auckland │ Haywards        │ 5018        ║
	╚══════════╧═════════╧════════════╧════════════════╧══════════════════╧═════════════════╧═════════════╝
	```

=== "CSV Output"

	```csv
	ID,Country,Region,State/Province,Time Zone,Locality,Postal Code
	11565134,NZ,,Wellington,Pacific/Auckland,Wellington City,
	11536889,NZ,Wellington,,Pacific/Auckland,Featherston,
	11536821,NZ,Wellington,,Pacific/Auckland,Haywards,5018
	```

=== "JSON Output"

	```json
	[
	  {
	    "id": 11565134,
	    "country": "NZ",
	    "stateOrProvince": "Wellington",
	    "locality": "Wellington City",
	    "zone": "Pacific/Auckland"
	  },
	  {
	    "id": 11536889,
	    "country": "NZ",
	    "region": "Wellington",
	    "locality": "Featherston",
	    "zone": "Pacific/Auckland"
	  },
	  {
	    "id": 11536821,
	    "country": "NZ",
	    "region": "Wellington",
	    "postalCode": "5018",
	    "locality": "Haywards",
	    "zone": "Pacific/Auckland"
	  }
	]
	```
