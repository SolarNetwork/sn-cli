---
title: renew
---
# Nodes Certificates Renew

Renew a node certificate.

## Usage

```
s10k nodes certificates renew -node=<nodeId> -p [-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to renew the certificate for |
| `-p=` | `--password=` | the certificate password; can omit value to be prompted interactively |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

Information about the renewed node certificate.

## Examples

=== "Renew certificate"

	```sh
	s10k nodes certificates renew --node-id 66 --password
	```

=== "Pretty Output"

	```
	+---------+--------+---------------------+---------------------+---------------------+------------+
	| Node ID | Status | Valid From          | Valid Until         | Renew After         | Renew Days |
	+---------+--------+---------------------+---------------------+---------------------+------------+
	|      66 | OK     | 2023-04-28T11:17:02 | 2033-04-25T11:17:02 | 2033-01-25T12:17:02 |       2673 |
	+---------+--------+---------------------+---------------------+---------------------+------------+
	```

=== "CSV Output"

	```csv
	Node ID,Status,Valid From,Valid Until,Renew After,Renew Days
	66,OK,2023-04-28T11:17:02,2033-04-25T11:17:02,2033-01-25T12:17:02,2673
	```

=== "JSON Output"

	```json
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
	}
	```
