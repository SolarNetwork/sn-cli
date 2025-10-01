---
title: create
---
# Nodes Certificates Create

Create node certificates.

Use this command to manually create node certificates, without going through the normal
[invitation/association process][node-setup]. This is useful for integrating custom data collection
applications with SolarNetwork that want to be able to post data, and thus require a certificate.

## Usage

```
s10k nodes certificates create [-tz=<zone>] -c=<country> -p=<password>
							[-d=<outputDirectory>]
							[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-c=` | `--country=` | a 2-character country code for the node's location |
| `-d=` | `--directory=` | the directory path to download the node's certificate to (will be created if necessary) |
| `-p=` | `--password=` | the password to use for the certificate keystore; can omit value to be prompted interactively |
| `-tz=` | `--time-zone=` | a time zone ID for the node's location instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

Details about the new node, including its assigned ID. If `--directory` and the `--verbose` options are used
the file path of the downloaded certificate will also be printed to standard error.

## Examples

=== "Create node certificate"

	```sh
	s10k nodes certificates create --country NZ --time-zone Pacific/Auckland --password
	```

=== "Output"

	```
	+---------+-----------------------------+--------+---------+------------------+
	| Node ID | Created                     | Public | Country | Time Zone        |
	+---------+-----------------------------+--------+---------+------------------+
	|      64 | 2023-04-27T23:15:58.795135Z | true   | NZ      | Pacific/Auckland |
	+---------+-----------------------------+--------+---------+------------------+
	```


[node-setup]: https://solarnetwork.github.io/solarnode-handbook/users/getting-started/#associate-your-solarnode-with-solarnetwork
[p12]: https://en.wikipedia.org/wiki/PKCS_12
