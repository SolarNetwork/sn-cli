---
title: create
---
# Sec-Tokens Create

Create a new security token.

## Usage

```
s10k sec-tokens create [-r] -t=<tokenType> [-n=<name>]
						[-D=<description>] [-node=nodeId[,nodeId...]]...
						[-source=sourceId[,sourceId...]]...
						[-N=metaPath[,metaPath...]]...
						[-U=metaPath[,metaPath...]]...
						[-A=path[,path...]]...
						[-exp=<expirationDate>] [-tz=<zone>]
						[-mode=<displayMode>]
						[-agg=aggregation |
							--aggregation=aggregation[,aggregation...]...]
						[-loc=precision |
							--location-precision=precision[,precision...]...]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-A=` | `--api-path=` | an API path(s) to restrict access to ([wildcard patterns][wildcard-pats] allowed) |
| `-agg=` | `--min-aggregation=` | a minimum [aggregation][aggregation] to restrict access to |
|   | `--aggregation=` | an [aggregation][aggregation] to restrict access to |
| `-D=` | `--description=` | a description for the token |
| `-exp=` | `--expiration-date=` | an expiration date for the token,like `2020-10-30` or `2020-10-30T12:45` |
| `-loc=` | `--min-location-precision=` | a minimum location precision to restrict access to |
|   | `--location-precision=` | a location precision to restrict access to |
| `-n=` | `--name=` | a brief name for the token |
| `-N=` | `--node-metadata-path=` | node [metadata paths][metadata-paths] to restrict access to ([wildcard patterns][wildcard-pats] allowed) |
| `-node=` | `--node-id=` | node ID(s) to restrict access to |
| `-r` | `--refresh-allowed` | allow signing keys for the token to be refreshed |
| `-source=` | `--source-id=` | source ID(s) to restrict access to |
| `-t=` | `--type=` | the type of token to create, one of `ReadNodeData` or `User` |
| `-tz=` | `--time-zone=` | a time zone ID to treat expiration date as instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-U=` | `--user-metadata-path=` |  user [metadata paths][metadata-paths] to restrict access to ([wildcard patterns][wildcard-pats] allowed) |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |


</div>

## Output

The new security token, with its associated secret.

!!! warning

	You must copy the **token secret** to a safe place, as SolarNetwork will not show you its value
	ever again.

## Examples

Create a basic `ReadNodeData` token with no policy restrictions:

=== "Create basic token"

	```sh
	s10k sec-tokens create --type ReadNodeData
	```

=== "Pretty Output"

	```
	+----------------------+---------------------------+--------------------------------+---------+--------------+------+-------------+--------+
	| Token ID             | Token Secret              | Created                        | User ID | Type         | Name | Description | Policy |
	+----------------------+---------------------------+--------------------------------+---------+--------------+------+-------------+--------+
	| ZCv5FUjlTlIQGjigLhSW | i.3R5qMQ-4sKg8uUV0XoxBY5y | 2025-09-29T03:51:32.388051375Z |     123 | ReadNodeData |      |             |        |
	+----------------------+---------------------------+--------------------------------+---------+--------------+------+-------------+--------+
	```

=== "CSV Output"

	```csv
	Token ID,Token Secret,Created,User ID,Type,Name,Description,Policy
	Lqc8Z_KzpD8.L0_em0Au,QlmQCrZaemdXmUNISggGF7ed,2025-09-29T03:52:13.248686631Z,123,ReadNodeData,,,
	```

=== "JSON Output"

	```json
	{
		"id" : "WoRxAChcFZ.x7s2zT5Y6",
		"authSecret" : "zcqVddxS0YTr6rzAtSA.Wq.x0tRzZL",
		"created" : "2025-09-29 04:13:52.673034405Z",
		"userId" : 123,
		"status" : "Active",
		"type" : "ReadNodeData",
		"expired" : false
	}
	```

Create a `User` token with a name, description, and various policy restrictions:

=== "Create token with policy"

	```sh
	s10k sec-tokens create --type User --name 'Reporting: region 1' \
		--description 'Allow access to region 1 for reporting team.' \
		--node-id 100,101 --source-id '/REGION1/**' \
		--min-aggregation Hour \
		--refresh-allowed
	```

=== "Pretty Output"

	```
	+----------------------+----------------------------+--------------------------------+---------+------+---------------------+----------------------------------------------+------------------------------------+
	| Token ID             | Token Secret               | Created                        | User ID | Type | Name                | Description                                  | Policy                             |
	+----------------------+----------------------------+--------------------------------+---------+------+---------------------+----------------------------------------------+------------------------------------+
	| kF67p6g30eUXQICUgzQf | PDX3VpOKmrc58HOOqUOv3HTmv0 | 2025-09-29T04:23:20.302916611Z |     123 | User | Reporting: region 1 | Allow access to region 1 for reporting team. | {                                  |
	|                      |                            |                                |         |      |                     |                                              |   "nodeIds" : [ 100, 101 ],        |
	|                      |                            |                                |         |      |                     |                                              |   "sourceIds" : [ "/REGION1/**" ], |
	|                      |                            |                                |         |      |                     |                                              |   "minAggregation" : "Hour",       |
	|                      |                            |                                |         |      |                     |                                              |   "refreshAllowed" : true          |
	|                      |                            |                                |         |      |                     |                                              | }                                  |
	+----------------------+----------------------------+--------------------------------+---------+------+---------------------+----------------------------------------------+------------------------------------+
	```

=== "CSV Output"

	```csv
	Token ID,Token Secret,Created,User ID,Type,Name,Description,Policy
	vWLOCdFTaB8IeF6__bl5,HQyeON_XND0YgkYlMHGz.7qPoYlP,2025-09-29T04:25:03.335116516Z,123,User,Reporting: region 1,Allow access to region 1 for reporting team.,"{
	  ""nodeIds"" : [ 100, 101 ],
	  ""sourceIds"" : [ ""/REGION1/**"" ],
	  ""minAggregation"" : ""Hour"",
	  ""refreshAllowed"" : true
	}"
	```

=== "JSON Output"

	```json
	{
		"id" : "0a.vDUsIQFDM.pOShkqf",
		"authSecret" : "mvBMxQukn1TsvmyjxQb6lWEZyebUWp",
		"created" : "2025-09-29 04:25:43.884356539Z",
		"userId" : 123,
		"name" : "Reporting: region 1",
		"description" : "Allow access to region 1 for reporting team.",
		"status" : "Active",
		"type" : "User",
		"expired" : false,
		"policy" : {
			"nodeIds" : [ 100, 101 ],
			"sourceIds" : [ "/REGION1/**" ],
			"minAggregation" : "Hour",
			"refreshAllowed" : true
		}
	}
	```


[aggregation]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarQuery-API-enumerated-types#aggregation-types
[metadata-paths]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#metadata-filter-key-paths
[wildcard-pats]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#wildcard-patterns
