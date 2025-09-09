---
title: update
---
# Instructions Settings Update

Update the settings on a node. Use the [`list-components`](../list-components.md) and
[`list-services`](../list-services.md) commands to discover the available services,
and [`settings view`](./view.md) to view the current settings on a service.

## Usage

```
s10k instructions settings update [-s=<serviceId>] [-c=<componentId>]
       							-node=<nodeId> [<setting>...]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to list the control IDs for |
| `-s=` | `--service-id=` | a service ID, or if `-c` provided the component instance ID, to view the settings for |
| `-c=` | `--component-id=` | a component ID to list the available service instances for |

</div>

## Parameters

Provide either key and value setting pairs (requires the `--service-id` and optionally `--component-id` options)
or the path to a CSV file, prefixed by `@@`.

Alternatively you can provide CSV via standard input.

## Output

A result status message.

## Examples

You can update one or more settings for a specific service on a node by providing `--service-id`
and optionally `--component-id` options along with pairs of key and value setting parameters. The
following example will update the `schedule` setting of a _Control Datum Source_ component
with ID `1` to "every 30 seconds":

```sh title="Update schedule for component instance to 30s"
s10k instructions settings update --node-id 101 \
	--component-id net.solarnetwork.node.datum.control \
	--service-id 1 \
	schedule '0/30 * * * * *'
```

Alternatively you can update settings for a single component from a CSV file like this (extra
columns are allowed and will be ignored). The following example updates two settings of a
_Control Datum Source_ component with ID `1`:

=== "Update specific service settings from CSV"

	```sh
	s10k instructions settings update --node-id 101 \
		--component-id net.solarnetwork.node.datum.control \
		--service-id 1 \
		@@my-service.csv
	```
=== "CSV input"

	```csv title="my-service.csv"
	Key,Value
	schedule,0/30 * * * * *
	jobService.multiDatumDataSource.eventModeValue,CaptureAndChange
	```

You can also provide a [full settings csv][settings-csv] file to update the settings on
an arbitrary number of [service][services] and [component][components] instances. The following
example updates settings on two different components:

=== "Update multiple services from CSV"

	```sh
	s10k instructions settings update --node-id 101 @@my-settings.csv
	```

=== "CSV input"

	```csv title="my-settings.csv"
	key,type,value
	net.solarnetwork.node.datum.control.1,jobService.multiDatumDataSource.eventModeValue,CaptureAndChange
	net.solarnetwork.node.datum.control.1,jobService.multiDatumDataSource.persistModeValue,Poll
	net.solarnetwork.node.datum.control.1,schedule,0/30 * * * * *
	net.solarnetwork.node.datum.control.FACTORY,1,1
	net.solarnetwork.node.control.mock.Limit,controlId,limit/1
	net.solarnetwork.node.control.mock.Limit,controlTypeValue,f
	net.solarnetwork.node.control.mock.Limit,initialControlValue,0
	net.solarnetwork.node.control.mock.FACTORY,Limit,Limit
	```


[components]: https://solarnetwork.github.io/solarnode-handbook/users/setup-app/settings/components/
[services]: https://solarnetwork.github.io/solarnode-handbook/users/setup-app/settings/services/
[settings-csv]: https://solarnetwork.github.io/solarnode-handbook/users/settings/
