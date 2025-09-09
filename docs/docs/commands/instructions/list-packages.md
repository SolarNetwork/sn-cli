---
title: list-packages
---
# Instructions List-Packages

List the installed and/or available software packages on a node.

## Usage

```
s10k instructions list-packages -node=<nodeId> [-filter=<filter>]
                                [-s=<packageStatus>]
                                [-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to list the control IDs for |
| `-filter=` | `--filter=` | a regular expression to apply against package names to restrict the results to; defaults to `^(sn-|solarnode)` |
| `-s` | `--status` | the package status to restrict results to, one of `Installed`, `Available`, or `All`; defaults to `Installed` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of all matching package records.

## Examples

You can list all installed packages on a node like this:

=== "Show installed packages"

	```sh
	s10k instructions list-packages --node-id 101
	```

=== "Pretty output"

	```
	+--------------------------------------+----------+-----------+
	| Name                                 | Version  | Installed |
	+--------------------------------------+----------+-----------+
	| sn-mbpoll                            |  1.5.2-1 |      true |
	+--------------------------------------+----------+-----------+
	| sn-nftables                          |  1.1.2-1 |      true |
	+--------------------------------------+----------+-----------+
	| sn-osstat                            |  1.1.0-2 |      true |
	+--------------------------------------+----------+-----------+
	| sn-pi                                |  1.2.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| sn-pi-usb-support                    |  1.2.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| sn-solarpkg                          |  1.3.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| sn-solarssh                          |  1.0.0-4 |      true |
	+--------------------------------------+----------+-----------+
	| sn-system                            |  1.7.1-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-control-core           |  2.1.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-core                   |  4.0.2-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-datumfilters           |  2.0.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-db-h2                  |  3.0.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-io-modbus              |  4.0.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-io-modbus-nifty-pjc    |  2.0.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-io-mqtt                |  5.0.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-io-mqtt-netty          |  5.0.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-modbus                 |  2.1.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-app-solarflux              |  2.0.0-1 |      true |
	+--------------------------------------+----------+-----------+
	| solarnode-base                       |  5.0.0-2 |      true |
	+--------------------------------------+----------+-----------+
	```

=== "CSV output"

	```csv
	Name,Version,Installed
	sn-mbpoll,1.5.2-1,true
	sn-nftables,1.1.2-1,true
	sn-osstat,1.1.0-2,true
	sn-pi,1.2.0-1,true
	sn-pi-usb-support,1.2.0-1,true
	sn-solarpkg,1.3.0-1,true
	sn-solarssh,1.0.0-4,true
	sn-system,1.7.1-1,true
	solarnode-app-control-core,2.1.0-1,true
	solarnode-app-core,4.0.2-1,true
	solarnode-app-datumfilters,2.0.0-1,true
	solarnode-app-db-h2,3.0.0-1,true
	solarnode-app-io-modbus,4.0.0-1,true
	solarnode-app-io-modbus-nifty-pjc,2.0.0-1,true
	solarnode-app-io-mqtt,5.0.0-1,true
	solarnode-app-io-mqtt-netty,5.0.0-1,true
	solarnode-app-modbus,2.1.0-1,true
	solarnode-app-solarflux,2.0.0-1,true
	solarnode-base,5.0.0-2,true
	```

=== "JSON output"

	```json
	[
		{
			"name": "sn-mbpoll",
			"version": "1.5.2-1",
			"installed": true
		},
		{
			"name": "sn-nftables",
			"version": "1.1.2-1",
			"installed": true
		},
		{
			"name": "sn-osstat",
			"version": "1.1.0-2",
			"installed": true
		},
		{
			"name": "sn-pi",
			"version": "1.2.0-1",
			"installed": true
		},
		{
			"name": "sn-pi-usb-support",
			"version": "1.2.0-1",
			"installed": true
		},
		{
			"name": "sn-solarpkg",
			"version": "1.3.0-1",
			"installed": true
		},
		{
			"name": "sn-solarssh",
			"version": "1.0.0-4",
			"installed": true
		},
		{
			"name": "sn-system",
			"version": "1.7.1-1",
			"installed": true
		},
		{
			"name": "solarnode-app-control-core",
			"version": "2.1.0-1",
			"installed": true
		},
		{
			"name": "solarnode-app-core",
			"version": "4.0.2-1",
			"installed": true
		},
		{
			"name": "solarnode-app-datumfilters",
			"version": "2.0.0-1",
			"installed": true
		},
		{
			"name": "solarnode-app-db-h2",
			"version": "3.0.0-1",
			"installed": true
		},
		{
			"name": "solarnode-app-io-modbus",
			"version": "4.0.0-1",
			"installed": true
		},
		{
			"name": "solarnode-app-io-modbus-nifty-pjc",
			"version": "2.0.0-1",
			"installed": true
		},
		{
			"name": "solarnode-app-io-mqtt",
			"version": "5.0.0-1",
			"installed": true
		},
		{
			"name": "solarnode-app-io-mqtt-netty",
			"version": "5.0.0-1",
			"installed": true
		},
		{
			"name": "solarnode-app-modbus",
			"version": "2.1.0-1",
			"installed": true
		},
		{
			"name": "solarnode-app-solarflux",
			"version": "2.0.0-1",
			"installed": true
		},
		{
			"name": "solarnode-base",
			"version": "5.0.0-2",
			"installed": true
		},
	]
	```
