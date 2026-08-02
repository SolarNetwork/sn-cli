---
title: list
---
# Nodes List

List node information matching search criteria.

## Usage

```
s10k nodes list
	[-node=nodeId[,nodeId...]]...
	[-m=name[,name...]]...
	[-c=<country>]
    [-tz=<zone>]
	[-loc=locId[,locId...]]...
	[-sort=orderKey[,orderKey...]]...
	[-M=max] [-O=<resultOffset>]
	[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-c=` | `--country=` | a country code to match, like `NZ` or `US` |
| `-loc=` | `--location-id=` | the location ID(s) to match |
| `-M=` | `--max=` | the maximum number of records to return |
| `-m=` | `--name=` | a case-insensitive name or description substring ot match |
| `-node=` | `--node-id=` | the node ID(s) to match |
| `-O=` | `--offset=` | start returning records from this offset, `0` being the first record |
| `-sort=` | `--sort-by=` | the sort key(s) to order the results by, one of `created`, `name`, `node`, or `zone`; note that `zone` orders the results based on the time zone ID (name), not its offset from UTC |
| `-tz=` | `--time-zone=` | a time zone ID to match, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of all available nodes. Each record contains the following properties:

| Property | Description |
|:---------|:------------|
| **Name** | An optional friendly name. In `CSV` and `PRETTY` modes this will be combined with the **Description**, if available. |
| **Description** | An optional friendly description. Only provided in `JSON` mode. |
| **Created** | The creation date of the node entity. |
| **Location ID** | The ID of the associated SolarNetwork location entity. |
| **Country** | The associated location's country code, for example `NZ` or `US`. |
| **Time Zone** | The associated location's time zone ID, for example `Pacific/Auckland` or `UTC`. |
| **Public** | Boolean flag indicating if the node's datum data requires a security token to access (`false`) or is publically available (`true`). In `JSON` mode this property is logically reversed as a `requiresAuthorization` property. |

## Examples

=== "List nodes"

	```sh
	s10k nodes list
	```

=== "Pretty Output"

	```
	+---------+---------------+----------------------------------+-------------+---------+------------------+--------+
	| Node ID | Name          | Created                          | Location ID | Country | Time Zone        | Public |
	+---------+---------------+----------------------------------+-------------+---------+------------------+--------+
	|       2 |               | 2025-04-20 07:07:32.384258+12:00 |    11536868 | NZ      | Pacific/Auckland | true   |
	+---------+---------------+----------------------------------+-------------+---------+------------------+--------+
	|       3 |               | 2025-04-20 07:07:32.384258+12:00 |    11536868 | NZ      | Pacific/Auckland | true   |
	+---------+---------------+----------------------------------+-------------+---------+------------------+--------+
	|       4 |               | 2025-10-01 14:07:19.655095+13:00 |    11532079 | NZ      | Pacific/Auckland | true   |
	+---------+---------------+----------------------------------+-------------+---------+------------------+--------+
	|       5 |               | 2025-10-01 14:11:15.964472+13:00 |    11532079 | NZ      | Pacific/Auckland | true   |
	+---------+---------------+----------------------------------+-------------+---------+------------------+--------+
	|      10 | New York site | 2025-08-06 10:10:13.00644+12:00  |        1000 | US      | America/New_York | false  |
	+---------+---------------+----------------------------------+-------------+---------+------------------+--------+
	```

=== "CSV Output"

	```csv
	Node ID,Name,Created,Location ID,Country,Time Zone,Public
	2,,2025-04-20 07:07:32.384258+12:00,11536868,NZ,Pacific/Auckland,true
	3,,2025-04-20 07:07:32.384258+12:00,11536868,NZ,Pacific/Auckland,true
	4,,2025-10-01 14:07:19.655095+13:00,11532079,NZ,Pacific/Auckland,true
	5,,2025-10-01 14:11:15.964472+13:00,11532079,NZ,Pacific/Auckland,true
	10,New York site,2025-08-06 10:10:13.00644+12:00,1000,US,America/New_York,false
	```

=== "JSON Output"

	```json
	[
	  {
	    "userId": 123,
	    "nodeId": 2,
	    "locationId": 11536868,
	    "country": "NZ",
	    "timeZone": "Pacific/Auckland",
	    "requiresAuthorization": false,
	    "created": "2025-04-19 19:07:32.384258Z"
	  },
	  {
	    "userId": 123,
	    "nodeId": 3,
	    "locationId": 11536868,
	    "country": "NZ",
	    "timeZone": "Pacific/Auckland",
	    "requiresAuthorization": false,
	    "created": "2025-04-19 19:07:32.384258Z"
	  },
	  {
	    "userId": 123,
	    "nodeId": 4,
	    "locationId": 11532079,
	    "country": "NZ",
	    "timeZone": "Pacific/Auckland",
	    "requiresAuthorization": false,
	    "created": "2025-10-01 01:07:19.655095Z"
	  },
	  {
	    "userId": 123,
	    "nodeId": 5,
	    "locationId": 11532079,
	    "country": "NZ",
	    "timeZone": "Pacific/Auckland",
	    "requiresAuthorization": false,
	    "created": "2025-10-01 01:11:15.964472Z"
	  },
	  {
	    "userId": 123,
	    "nodeId": 10,
	    "name": "New York site",
	    "locationId": 1000,
	    "country": "US",
	    "timeZone": "America/New_York",
	    "requiresAuthorization": true,
	    "created": "2025-08-05 22:10:13.00644Z"
	  }
	]
	```
