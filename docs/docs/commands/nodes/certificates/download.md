---
title: download
---
# Nodes Certificates Download

Download a node certificate as a [PKCS#12][p12] keystore with a node's private key and signed public
certificate. The keystore is encrypted with the certificate password given when the certificate was
created. SolarNetwork does not retain this password, so it is up to users/client applications to
provide it when needed.

## Usage

```
s10k nodes certificates download -node=nodeId[,nodeId...]
								[-node=nodeId[,nodeId...]]...
								[-d=<outputDirectory>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID(s) to download certificates for |
| `-d=` | `--directory=` | the directory path to save the certificates to (will be created if necessary); if not provided the current working directory will be used |

</div>

## Output

If the `--verbose` option used, a listing of the certificate file paths that were downloaded.

## Examples

=== "Verbose download certificates"

	```sh
	s10k --verbose nodes certificates download --directory certs --node-id 66,70
	```

=== "Output"

	```
	certs/solarnode-66.p12
	certs/solarnode-70.p12
	```


[p12]: https://en.wikipedia.org/wiki/PKCS_12
