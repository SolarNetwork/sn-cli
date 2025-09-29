---
title: list
---
# Sec-Tokens List

List security tokens matching a search filter.

## Usage

```
s10k sec-tokens list [-id=tokenId[,tokenId...]]... [-t=<tokenType>]
                    [-mode=<displayMode>] [-a | -d]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-a` | `--active` | show only active tokens |
| `-d` | `--disabled` | show only disabled tokens |
| `-id=` | `--identifier=` | the ID(s) of the tokens to show |
| `-t=` | `--type=` | the type of token to show, one of `ReadNodeData` or `User` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of security tokens matching the search criteria.

## Examples

List all `ReadNodeData` tokens:

=== "List `ReadNodeData` tokens"

	```sh
	s10k sec-tokens list --type ReadNodeData
	```

=== "Pretty Output"

	```
	+----------------------+-----------------------------+---------+--------------+----------+----------------+-------------------------------------+------------------------------------------------------+
	| Token ID             | Created                     | User ID | Type         | Status   | Name           | Description                         | Policy                                               |
	+----------------------+-----------------------------+---------+--------------+----------+----------------+-------------------------------------+------------------------------------------------------+
	| Lqc8Z_KzpD8.L0_em0Au | 2025-09-29T03:52:13.248687Z |     123 | ReadNodeData | Active   |                |                                     |                                                      |
	+----------------------+-----------------------------+---------+--------------+----------+----------------+-------------------------------------+------------------------------------------------------+
	| zLgbyhRvcMhQ83QRFJqh | 2025-09-29T04:05:46.562370Z |     123 | ReadNodeData | Disabled | Student access | Allow access to school system data. | {                                                    |
	|                      |                             |         |              |          |                |                                     |   "nodeIds" : [ 100, 101 ],                          |
	|                      |                             |         |              |          |                |                                     |   "sourceIds" : [ "mock/**", "test/**" ],            |
	|                      |                             |         |              |          |                |                                     |   "minAggregation" : "Hour",                         |
	|                      |                             |         |              |          |                |                                     |   "minLocationPrecision" : "PostalCode",             |
	|                      |                             |         |              |          |                |                                     |   "nodeMetadataPaths" : [ "/pm/test/**" ],           |
	|                      |                             |         |              |          |                |                                     |   "userMetadataPaths" : [ "/pm/account/public/**" ], |
	|                      |                             |         |              |          |                |                                     |   "notAfter" : 1759143600000,                        |
	|                      |                             |         |              |          |                |                                     |   "refreshAllowed" : true                            |
	|                      |                             |         |              |          |                |                                     | }                                                    |
	+----------------------+-----------------------------+---------+--------------+----------+----------------+-------------------------------------+------------------------------------------------------+
	```

=== "CSV Output"

	```csv
	Token ID,Created,User ID,Type,Status,Name,Description,Policy
	Lqc8Z_KzpD8.L0_em0Au,2025-09-29T03:52:13.248687Z,123,ReadNodeData,Active,,,
	zLgbyhRvcMhQ83QRFJqh,2025-09-29T04:05:46.562370Z,123,ReadNodeData,Disabled,Student access,Allow access to school system data.,"{
	  ""nodeIds"" : [ 100, 101 ],
	  ""sourceIds"" : [ ""mock/**"", ""test/**"" ],
	  ""minAggregation"" : ""Hour"",
	  ""minLocationPrecision"" : ""PostalCode"",
	  ""nodeMetadataPaths"" : [ ""/pm/test/**"" ],
	  ""userMetadataPaths"" : [ ""/pm/account/public/**"" ],
	  ""notAfter"" : 1759143600000,
	  ""refreshAllowed"" : true
	}"
	```

=== "JSON Output"

	```json
	[
		{
			"id" : "Lqc8Z_KzpD8.L0_em0Au",
			"created" : "2025-09-29 03:52:13.248687Z",
			"userId" : 123,
			"status" : "Active",
			"type" : "ReadNodeData",
			"expired" : false
		},
		{
			"id" : "zLgbyhRvcMhQ83QRFJqh",
			"created" : "2025-09-29 04:05:46.56237Z",
			"userId" : 123,
			"name" : "Student access",
			"description" : "Allow access to school system data.",
			"status" : "Disabled",
			"type" : "ReadNodeData",
			"expired" : false,
			"policy" : {
				"nodeIds" :   [ 100, 101 ],
				"sourceIds" :   [ "mock/**", "test/**" ],
				"minAggregation" : "Hour",
				"minLocationPrecision" : "PostalCode",
				"nodeMetadataPaths" :   [ "/pm/test/**" ],
				"userMetadataPaths" :   [ "/pm/account/public/**" ],
				"notAfter" : 1759143600000,
				"refreshAllowed" : true
			}
		}
	]
	```
