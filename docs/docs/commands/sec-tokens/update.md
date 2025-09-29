---
title: update
---
# Sec-Tokens Update

Update a security token.

## Usage

```
s10k sec-tokens update [-rR] -id=tokenId [-n=<name>]
						[-D=<description>] [-node=nodeId[,nodeId...]]...
						[-source=sourceId[,sourceId...]]...
						[-N=metaPath[,metaPath...]]...
						[-U=metaPath[,metaPath...]]...
						[-A=path[,path...]]...
						[-exp=<expirationDate>] [-tz=<zone>]
						[-mode=<displayMode>]
						[-a | -d]
						[-agg=aggregation |
							--aggregation=aggregation[,aggregation...]...]
						[-loc=precision |
							--location-precision=precision[,precision...]...]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-a` | `--active` | make the token active |
| `-A=` | `--api-path=` | an API path(s) to restrict access to ([wildcard patterns][wildcard-pats] allowed) |
| `-agg=` | `--min-aggregation=` | a minimum [aggregation][aggregation] to restrict access to |
|   | `--aggregation=` | an [aggregation][aggregation] to restrict access to |
| `-d` | `--disabled` | make the token disabled |
| `-D=` | `--description=` | a description for the token |
| `-exp=` | `--expiration-date=` | an expiration date for the token,like `2020-10-30` or `2020-10-30T12:45` |
| `-id=` | `--identifier=` | the ID of the token to update |
| `-loc=` | `--min-location-precision=` | a minimum location precision to restrict access to |
|   | `--location-precision=` | a location precision to restrict access to |
| `-n=` | `--name=` | a brief name for the token |
| `-N=` | `--node-metadata-path=` | node [metadata paths][metadata-paths] to restrict access to ([wildcard patterns][wildcard-pats] allowed) |
| `-node=` | `--node-id=` | node ID(s) to restrict access to |
| `-r` | `--refresh-allowed` | allow signing keys for the token to be refreshed |
| `-R` | `--replace` | replace the security policy, instead of updating (adding to) the policy |
| `-source=` | `--source-id=` | source ID(s) to restrict access to |
| `-t=` | `--type=` | the type of token to create, one of `ReadNodeData` or `User` |
| `-tz=` | `--time-zone=` | a time zone ID to treat expiration date as instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-U=` | `--user-metadata-path=` |  user [metadata paths][metadata-paths] to restrict access to ([wildcard patterns][wildcard-pats] allowed) |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |


</div>

## Output

The updated security token.

## Examples

Update the name and description of a token:

=== "Update token name"

	```sh
	s10k sec-tokens update --identifier hNAdH.aXHaEqFossMwT8 \
		--name 'Student access' \
		--description 'Allow access to school system data.'
	```

=== "Pretty Output"

	```
	+----------------------+-----------------------------+---------+--------------+--------+----------------+-------------------------------------+--------+
	| Token ID             | Created                     | User ID | Type         | Status | Name           | Description                         | Policy |
	+----------------------+-----------------------------+---------+--------------+--------+----------------+-------------------------------------+--------+
	| hNAdH.aXHaEqFossMwT8 | 2025-09-29T03:52:43.281061Z |     123 | ReadNodeData | Active | Student access | Allow access to school system data. |        |
	+----------------------+-----------------------------+---------+--------------+--------+----------------+-------------------------------------+--------+
	```

=== "CSV Output"

	```csv
	Token ID,Created,User ID,Type,Status,Name,Description,Policy
	hNAdH.aXHaEqFossMwT8,2025-09-29T03:52:43.281061Z,123,ReadNodeData,Active,Student access,Allow access to school system data.,
	```

=== "JSON Output"

	```json
	{
		"id" : "hNAdH.aXHaEqFossMwT8",
		"created" : "2025-09-29 03:52:43.281061Z",
		"userId" : 123,
		"name" : "Student access",
		"description" : "Allow access to school system data.",
		"status" : "Active",
		"type" : "ReadNodeData",
		"expired" : false
	}
	```

Add policy restrictions, preserving existing restrictions:

=== "Update token policy"

	```sh
	s10k sec-tokens update --identifier kF67p6g30eUXQICUgzQf \
		--node-id 107,164 \
		--source-id '/REGION2/**'
	```

=== "Pretty Output"

	```
	+----------------------+-----------------------------+---------+------+--------+---------------------+----------------------------------------------+---------------------------------------------------+
	| Token ID             | Created                     | User ID | Type | Status | Name                | Description                                  | Policy                                            |
	+----------------------+-----------------------------+---------+------+--------+---------------------+----------------------------------------------+---------------------------------------------------+
	| kF67p6g30eUXQICUgzQf | 2025-09-29T04:23:20.302917Z |     123 | User | Active | Reporting: region 1 | Allow access to region 1 for reporting team. | {                                                 |
	|                      |                             |         |      |        |                     |                                              |   "nodeIds" : [ 100, 101, 107, 164 ],             |
	|                      |                             |         |      |        |                     |                                              |   "sourceIds" : [ "/REGION1/**", "/REGION2/**" ], |
	|                      |                             |         |      |        |                     |                                              |   "minAggregation" : "Hour",                      |
	|                      |                             |         |      |        |                     |                                              |   "refreshAllowed" : true                         |
	|                      |                             |         |      |        |                     |                                              | }                                                 |
	+----------------------+-----------------------------+---------+------+--------+---------------------+----------------------------------------------+---------------------------------------------------+
	```

=== "CSV Output"

	```csv
	Token ID,Created,User ID,Type,Status,Name,Description,Policy
	kF67p6g30eUXQICUgzQf,2025-09-29T04:23:20.302917Z,123,User,Active,Reporting: region 1,Allow access to region 1 for reporting team.,"{
	""nodeIds"" : [ 100, 101, 107, 164 ],
	""sourceIds"" : [ ""/REGION2/**"", ""/REGION1/**"" ],
	""minAggregation"" : ""Hour"",
	""refreshAllowed"" : true
	}"
	```

=== "JSON Output"

	```json
	{
		"id" : "kF67p6g30eUXQICUgzQf",
		"created" : "2025-09-29 04:23:20.302917Z",
		"userId" : 123,
		"name" : "Reporting: region 1",
		"description" : "Allow access to region 1 for reporting team.",
		"status" : "Active",
		"type" : "User",
		"expired" : false,
		"policy" : {
			"nodeIds" : [ 100, 101, 107, 164 ],
			"sourceIds" : [ "/REGION2/**", "/REGION1/**" ],
			"minAggregation" : "Hour",
			"refreshAllowed" : true
		}
	}
	```

Replace policy restrictions, discarding existing restrictions, with the `--replace` option:

=== "Update token policy"

	```sh
	s10k sec-tokens update --identifier kF67p6g30eUXQICUgzQf \
		--node-id 107,164 \
		--source-id '/REGION2/**' \
		--replace
	```

=== "Pretty Output"

	```
	+----------------------+-----------------------------+---------+------+--------+---------------------+----------------------------------------------+-----------------------------------+
	| Token ID             | Created                     | User ID | Type | Status | Name                | Description                                  | Policy                            |
	+----------------------+-----------------------------+---------+------+--------+---------------------+----------------------------------------------+-----------------------------------+
	| kF67p6g30eUXQICUgzQf | 2025-09-29T04:23:20.302917Z |     123 | User | Active | Reporting: region 1 | Allow access to region 1 for reporting team. | {                                 |
	|                      |                             |         |      |        |                     |                                              |   "nodeIds" : [ 107, 164 ],       |
	|                      |                             |         |      |        |                     |                                              |   "sourceIds" : [ "/REGION2/**" ] |
	|                      |                             |         |      |        |                     |                                              | }                                 |
	+----------------------+-----------------------------+---------+------+--------+---------------------+----------------------------------------------+-----------------------------------+
	```

=== "CSV Output"

	```csv
	Token ID,Created,User ID,Type,Status,Name,Description,Policy
	kF67p6g30eUXQICUgzQf,2025-09-29T04:23:20.302917Z,123,User,Active,Reporting: region 1,Allow access to region 1 for reporting team.,"{
	""nodeIds"" : [ 107, 164 ],
	""sourceIds"" : [ ""/REGION2/**"" ]
	}"
	```

=== "JSON Output"

	```json
	{
		"id" : "kF67p6g30eUXQICUgzQf",
		"created" : "2025-09-29 04:23:20.302917Z",
		"userId" : 123,
		"name" : "Reporting: region 1",
		"description" : "Allow access to region 1 for reporting team.",
		"status" : "Active",
		"type" : "User",
		"expired" : false,
		"policy" : {
			"nodeIds" : [ 107, 164 ],
			"sourceIds" : [ "/REGION2/**" ]
		}
	}
	```


[aggregation]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarQuery-API-enumerated-types#aggregation-types
[metadata-paths]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#metadata-filter-key-paths
[wildcard-pats]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#wildcard-patterns
