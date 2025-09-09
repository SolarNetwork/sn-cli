---
title: list-components
---
# Instructions List-Components

List the available [components][components] on a node.

## Usage

```
s10k instructions list-components -node=<nodeId> [-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to list the control IDs for |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of available component records.

## Examples

You can list all available components on a node like this:

=== "Show available components"

	```sh
	s10k instructions list-components --node-id 101
	```

=== "Pretty output"

	```
	+------------------------------------------------------+--------------------------------------------+
	| ID                                                   | Title                                      |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.control.modbus                 | Modbus Control                             |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.control.opmode.opstatemgr      | Operational State Manager                  |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.control.opmode.switch          | Operational Mode Switch                    |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.control                  | Control Datum Source                       |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.filter.control.update    | Control Updater Datum Filter               |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.filter.std.join          | Join Datum Filter                          |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.filter.std.param         | Parameter Expression Datum Filter          |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.filter.std.split         | Split Datum Filter                         |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.filter.std.unchanged     | Unchanged Datum Filter                     |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.filter.std.unchangedprop | Unchanged Property Filter                  |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.filter.tariff            | Time-based Tariff Datum Filter             |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.modbus                   | Modbus Device                              |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.opmode.invoker           | Datum Data Source Operational Mode Invoker |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.samplefilter.downsample  | Downsample Datum Filter                    |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.samplefilter.expression  | Expression Datum Filter                    |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.samplefilter.limiter     | Throttle Datum Filter                      |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.samplefilter.opmode      | Operational Mode Datum Filter              |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.samplefilter.prop        | Property Datum Filter                      |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.samplefilter.simple      | Datum Property Global Filter               |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.samplefilter.throttle    | Datum Throttle Global Filter               |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.samplefilter.virtmeter   | Virtual Meter Datum Filter                 |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.datum.xform.user               | Datum Filter Chain                         |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.io.modbus                      | Modbus serial connection                   |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.io.modbus.tcp                  | Modbus TCP connection                      |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.metadata.user                  | User Metadata Service                      |
	+------------------------------------------------------+--------------------------------------------+
	| net.solarnetwork.node.upload.flux                    | SolarFlux Upload Service                   |
	+------------------------------------------------------+--------------------------------------------+
	```

=== "CSV output"

	```csv
	ID,Title
	net.solarnetwork.node.control.modbus,Modbus Control
	net.solarnetwork.node.control.opmode.opstatemgr,Operational State Manager
	net.solarnetwork.node.control.opmode.switch,Operational Mode Switch
	net.solarnetwork.node.datum.control,Control Datum Source
	net.solarnetwork.node.datum.filter.control.update,Control Updater Datum Filter
	net.solarnetwork.node.datum.filter.std.join,Join Datum Filter
	net.solarnetwork.node.datum.filter.std.param,Parameter Expression Datum Filter
	net.solarnetwork.node.datum.filter.std.split,Split Datum Filter
	net.solarnetwork.node.datum.filter.std.unchanged,Unchanged Datum Filter
	net.solarnetwork.node.datum.filter.std.unchangedprop,Unchanged Property Filter
	net.solarnetwork.node.datum.filter.tariff,Time-based Tariff Datum Filter
	net.solarnetwork.node.datum.modbus,Modbus Device
	net.solarnetwork.node.datum.opmode.invoker,Datum Data Source Operational Mode Invoker
	net.solarnetwork.node.datum.samplefilter.downsample,Downsample Datum Filter
	net.solarnetwork.node.datum.samplefilter.expression,Expression Datum Filter
	net.solarnetwork.node.datum.samplefilter.limiter,Throttle Datum Filter
	net.solarnetwork.node.datum.samplefilter.opmode,Operational Mode Datum Filter
	net.solarnetwork.node.datum.samplefilter.prop,Property Datum Filter
	net.solarnetwork.node.datum.samplefilter.simple,Datum Property Global Filter
	net.solarnetwork.node.datum.samplefilter.throttle,Datum Throttle Global Filter
	net.solarnetwork.node.datum.samplefilter.virtmeter,Virtual Meter Datum Filter
	net.solarnetwork.node.datum.xform.user,Datum Filter Chain
	net.solarnetwork.node.io.modbus,Modbus serial connection
	net.solarnetwork.node.io.modbus.tcp,Modbus TCP connection
	net.solarnetwork.node.metadata.user,User Metadata Service
	net.solarnetwork.node.upload.flux,SolarFlux Upload Service
	```

=== "JSON output"

	```json
	[
		{
			"id": "net.solarnetwork.node.control.modbus",
			"title": "Modbus Control"
		},
		{
			"id": "net.solarnetwork.node.control.opmode.opstatemgr",
			"title": "Operational State Manager"
		},
		{
			"id": "net.solarnetwork.node.control.opmode.switch",
			"title": "Operational Mode Switch"
		},
		{
			"id": "net.solarnetwork.node.datum.control",
			"title": "Control Datum Source"
		},
		{
			"id": "net.solarnetwork.node.datum.filter.control.update",
			"title": "Control Updater Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.filter.std.join",
			"title": "Join Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.filter.std.param",
			"title": "Parameter Expression Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.filter.std.split",
			"title": "Split Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.filter.std.unchanged",
			"title": "Unchanged Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.filter.std.unchangedprop",
			"title": "Unchanged Property Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.filter.tariff",
			"title": "Time-based Tariff Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.modbus",
			"title": "Modbus Device"
		},
		{
			"id": "net.solarnetwork.node.datum.opmode.invoker",
			"title": "Datum Data Source Operational Mode Invoker"
		},
		{
			"id": "net.solarnetwork.node.datum.samplefilter.downsample",
			"title": "Downsample Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.samplefilter.expression",
			"title": "Expression Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.samplefilter.limiter",
			"title": "Throttle Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.samplefilter.opmode",
			"title": "Operational Mode Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.samplefilter.prop",
			"title": "Property Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.samplefilter.simple",
			"title": "Datum Property Global Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.samplefilter.throttle",
			"title": "Datum Throttle Global Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.samplefilter.virtmeter",
			"title": "Virtual Meter Datum Filter"
		},
		{
			"id": "net.solarnetwork.node.datum.xform.user",
			"title": "Datum Filter Chain"
		},
		{
			"id": "net.solarnetwork.node.io.modbus",
			"title": "Modbus serial connection"
		},
		{
			"id": "net.solarnetwork.node.io.modbus.tcp",
			"title": "Modbus TCP connection"
		},
		{
			"id": "net.solarnetwork.node.metadata.user",
			"title": "User Metadata Service"
		},
		{
			"id": "net.solarnetwork.node.upload.flux",
			"title": "SolarFlux Upload Service"
		}
	]
	```

[components]: https://solarnetwork.github.io/solarnode-handbook/users/setup-app/settings/components/
