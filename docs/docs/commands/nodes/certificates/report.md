---
title: report
---
# Nodes Certificates Report

Generate a report on node certificates, including their expiration dates.

Passwords for each certificate must be provided as a table of node ID and
password pairs. The data can be provided as CSV or JSON. For CSV it must include
a header row and column names that match the `--node-id-col` and `--password-col`
options, or exactly two columns one of which has node ID number values and the
other assumed to be passwords.

For JSON a top-level array is required, with either object elements with
property names that match the `--node-id-col` and `--password-col` options, or a
nested array with two elements in the form `[nodeId,"password"]`.

## Usage

```
s10k nodes certificates report [-n=<nodeIdColumnName>]
							[-p=<passwordColumnName>]
							[-N=<nodeIdRegex>] [-mode=<displayMode>]
							[<table>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-n=` | `--node-id-col=` | the name of the node ID CSV column or JSON property; defaults to `Node ID` |
| `-N=` | `--node-id-regex=` | a regular expression to extract the node ID with from CSV data; must provide a capture group for the node ID |
| `-p=` | `--password-col=` | the name of the certificate password CSV column or JSON property; defaults to `Certificate Password` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Parameters

Pass the CSV/JSON table data of certificate passwords as the one and only parameter.
Alternatively the data can be provided on standard input.

### CSV table format

For CSV input you can either provide exactly two columns in the table and the command will
figure out which column is for node IDs and which is for passwords from the data itself.
Otherwise use the `--node-id-col` and `--password-col` options to specify the name of the
column to use for both. Either way a single header row of the column names is required.

The `--node-id-regex` option can be used to extract the node ID value from that column's
values. The regular expression must provide a capture group (an expression surrounded by
`(` and `)`) to provide the node ID value.

For example, if the node ID cell values are like `Node 123` then
`--node-id-regex="Node (\d+)"` would work to extract the ID value.

### JSON table format

For JSON input you must provide a JSON array, the elements of which can be one of two options:

 1. a JSON object, with property names equal to the `--node-id-col` and `--password-col` options
 2. a JSON array, with exactly 2 elements: the node ID and the certificate password, in that order

For example, these two JSON values would be equivalent, assuming you passed
`--node-id-col=nodeId --password-col=certificatePassword` options for the object syntax:

=== "JSON objects"

	```json
	[
		{"nodeId": 66, "certificatePassword": "password1"},
		{"nodeId": 70, "certificatePassword": "password2"},
		{"nodeId": 100, "certificatePassword": "password3"},
		{"nodeId": 101, "certificatePassword": "password4"}
	]
	```

=== "JSON arrays"

	```json
	[
		[66,"password1"],
		[70,"password2"],
		[100,"password3"],
		[101,"password4"]
	]
	```

## Output

A listing of all available node certificates.

## Examples

=== "Report with CSV data"

	```sh
	s10k nodes certificates report <certificate-passwords.csv
	```

=== "certificate-passwords.csv file"

	```csv
	Node ID,Certificate Password
	66,password1
	70,password2
	100,password3
	101,password4
	```

=== "Pretty Output"

	```
	+---------+--------+---------------------+---------------------+---------------------+------------+
	| Node ID | Status | Valid From          | Valid Until         | Renew After         | Renew Days |
	+---------+--------+---------------------+---------------------+---------------------+------------+
	|      66 | OK     | 2023-04-28T11:17:02 | 2033-04-25T11:17:02 | 2033-01-25T12:17:02 |       2673 |
	+---------+--------+---------------------+---------------------+---------------------+------------+
	|      70 | OK     | 2024-05-17T07:03:58 | 2034-05-15T07:03:58 | 2034-02-15T08:03:58 |       3059 |
	+---------+--------+---------------------+---------------------+---------------------+------------+
	|     100 | OK     | 2025-07-08T14:22:38 | 2035-07-06T14:22:38 | 2035-04-06T14:22:38 |       3474 |
	+---------+--------+---------------------+---------------------+---------------------+------------+
	|     101 | OK     | 2025-07-08T15:24:26 | 2035-07-06T15:24:26 | 2035-04-06T15:24:26 |       3474 |
	+---------+--------+---------------------+---------------------+---------------------+------------+
	```

=== "CSV Output"

	```csv
	Node ID,Status,Valid From,Valid Until,Renew After,Renew Days
	66,OK,2023-04-28T11:17:02,2033-04-25T11:17:02,2033-01-25T12:17:02,2673
	70,OK,2024-05-17T07:03:58,2034-05-15T07:03:58,2034-02-15T08:03:58,3059
	100,OK,2025-07-08T14:22:38,2035-07-06T14:22:38,2035-04-06T14:22:38,3474
	101,OK,2025-07-08T15:24:26,2035-07-06T15:24:26,2035-04-06T15:24:26,3474
	```

=== "JSON Output"

	```json
	[
	  {
		"userId": 123,
		"nodeId": 66,
		"serialNumber": 65889,
		"issuerDN": "CN=SolarNetwork Root CA,OU=SolarNetwork Certification Authority,O=SolarNetwork",
		"subjectDN": "UID=664,O=SolarNetwork",
		"validFromDate": "2023-04-27 23:17:02Z",
		"validUntilDate": "2033-04-24 23:17:02Z",
		"renewAfterDate": "2033-01-24 23:17:02Z",
		"status": "OK"
	  },
	  {
		"userId": 123,
		"nodeId": 70,
		"serialNumber": 65936,
		"issuerDN": "CN=SolarNetwork Root CA,OU=SolarNetwork Certification Authority,O=SolarNetwork",
		"subjectDN": "UID=707,O=SolarNetwork",
		"validFromDate": "2024-05-16 19:03:58Z",
		"validUntilDate": "2034-05-14 19:03:58Z",
		"renewAfterDate": "2034-02-14 19:03:58Z",
		"status": "OK"
	  },
	  {
		"userId": 123,
		"nodeId": 100,
		"serialNumber": 66250,
		"issuerDN": "CN=SolarNetwork Root CA,OU=SolarNetwork Certification Authority,O=SolarNetwork",
		"subjectDN": "UID=1010,O=SolarNetwork",
		"validFromDate": "2025-07-08 02:22:38Z",
		"validUntilDate": "2035-07-06 02:22:38Z",
		"renewAfterDate": "2035-04-06 02:22:38Z",
		"status": "OK"
	  },
	  {
		"userId": 123,
		"nodeId": 101,
		"serialNumber": 66251,
		"issuerDN": "CN=SolarNetwork Root CA,OU=SolarNetwork Certification Authority,O=SolarNetwork",
		"subjectDN": "UID=1011,O=SolarNetwork",
		"validFromDate": "2025-07-08 03:24:26Z",
		"validUntilDate": "2035-07-06 03:24:26Z",
		"renewAfterDate": "2035-04-06 03:24:26Z",
		"status": "OK"
	  }
	]
	```
