---
title: view-service
---
# Instructions List-Services

List the settings for a [component][components] instance or [service][services] on a node.
Use the [list-components](./list-components.md) and [list-services](./list-services.md) commands
to discover the available services.

When generating `CSV` output, the `--full-csv` option will cause valid [Settings CSV][settings-csv]
to be generated, which can be directly imported into SolarNode.

Additionally the **specification** for a service can be shown, which is useful for applications
dynamically discovering how to configure the settings.

!!! info

	For more information about setting specifications, see the [Developer Settings][dev-specs]
	section of the SolarNode Handbook.

## Usage

```
s10k instructions view-service [-FS] -node=<nodeId> -s=<serviceId>
                                [-c=<componentId>] [-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-F` | `--full-csv` | when `CSV` output is specified, output full [Settings CSV][settings-csv] suitable for importing into SolarNode |
| `-S` | `--specification` | show setting specifications instead of the current setting values; `JSON` output is implied |
| `-node=` | `--node-id=` | the node ID to list the control IDs for |
| `-s=` | `--service-id=` | a service ID, or if `-c` provided the component instance ID, to view the settings for |
| `-c=` | `--component-id=` | a component ID to list the available service instances for |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of service setting, or setting specification, records.

## Examples

You can view the settings for a service on a node like this:

=== "View service settings"

	```sh
	s10k instructions view-service --node-id 101 \
	  --service-id net.solarnetwork.node.metadata.json.JsonDatumMetadataService
	```

=== "Pretty output"

	```
	+---------------------------+-------+---------+
	| Key                       | Value | Default |
	+---------------------------+-------+---------+
	| updatePersistDelaySeconds | 2     |    true |
	+---------------------------+-------+---------+
	| updateThrottleSeconds     | 60    |    true |
	+---------------------------+-------+---------+
	```

=== "CSV output"

	```csv
	Key,Value,Default
	updatePersistDelaySeconds,2,true
	updateThrottleSeconds,60,true
	```

=== "JSON output"

	```json
	[
		{
			"key": "updatePersistDelaySeconds",
			"value": "2",
			"default": true
		},
		{
			"key": "updateThrottleSeconds",
			"value": "60",
			"default": true
		}
	]
	```

You can view the settings for a component instance with the `--component-id` option

=== "View component instance settings"

	```sh
	s10k instructions view-service --node-id 101 \
	  --component-id net.solarnetwork.node.datum.control \
	  --service-id 1
	```

=== "Pretty output"

	```
	+-----------------------------------------------------+--------------+---------+
	| Key                                                 | Value        | Default |
	+-----------------------------------------------------+--------------+---------+
	| schedule                                            | 0 * * * * ?  |    true |
	+-----------------------------------------------------+--------------+---------+
	| jobService.multiDatumDataSource.uid                 |              |    true |
	+-----------------------------------------------------+--------------+---------+
	| jobService.multiDatumDataSource.groupUid            |              |    true |
	+-----------------------------------------------------+--------------+---------+
	| jobService.multiDatumDataSource.controlIdRegexValue |              |    true |
	+-----------------------------------------------------+--------------+---------+
	| jobService.multiDatumDataSource.eventModeValue      | Change       |    true |
	+-----------------------------------------------------+--------------+---------+
	| jobService.multiDatumDataSource.persistModeValue    | PollAndEvent |   false |
	+-----------------------------------------------------+--------------+---------+
	```

=== "CSV output"

	```csv
	Key,Value,Default
	schedule,0 * * * * ?,true
	jobService.multiDatumDataSource.uid,,true
	jobService.multiDatumDataSource.groupUid,,true
	jobService.multiDatumDataSource.controlIdRegexValue,,true
	jobService.multiDatumDataSource.eventModeValue,Change,true
	jobService.multiDatumDataSource.persistModeValue,PollAndEvent,false
	```

=== "JSON output"

	```json
	[
		[
			{
				"key": "schedule",
				"value": "0 * * * * ?",
				"default": true
			},
			{
				"key": "jobService.multiDatumDataSource.uid",
				"value": "",
				"default": true
			},
			{
				"key": "jobService.multiDatumDataSource.groupUid",
				"value": "",
				"default": true
			},
			{
				"key": "jobService.multiDatumDataSource.controlIdRegexValue",
				"value": "",
				"default": true
			},
			{
				"key": "jobService.multiDatumDataSource.eventModeValue",
				"value": "Change",
				"default": true
			},
			{
				"key": "jobService.multiDatumDataSource.persistModeValue",
				"value": "PollAndEvent",
				"default": false
			}
		]
	]
	```

When generating `CSV` output, the `--full-csv` option will cause valid [Settings CSV][settings-csv]
to be generated, which can be directly imported into SolarNode:

=== "General full Settings CSV"

	```sh
	s10k instructions view-service --node-id 101 \
	  --component-id net.solarnetwork.node.datum.control \
	  --service-id 1 \
	  --display-mode CSV --full-csv
	```

=== "Output"

	!!! note

		Any settings that are set to a system default value will be commented out with a leading
		`#` character. If you want to change that setting value, be sure to un-comment the line
		by removing the leading `#`.

	```csv
	Key,Type,Value
	#net.solarnetwork.node.datum.control.1,schedule,0 * * * * ?
	#net.solarnetwork.node.datum.control.1,jobService.multiDatumDataSource.uid,
	#net.solarnetwork.node.datum.control.1,jobService.multiDatumDataSource.groupUid,
	#net.solarnetwork.node.datum.control.1,jobService.multiDatumDataSource.controlIdRegexValue,
	#net.solarnetwork.node.datum.control.1,jobService.multiDatumDataSource.eventModeValue,Change
	net.solarnetwork.node.datum.control.1,jobService.multiDatumDataSource.persistModeValue,PollAndEvent
	net.solarnetwork.node.datum.control.FACTORY,1,1
	```

You can view the setting specification for a service with the `--specification` option,
to understand more about how each setting is configured:

=== "View component instance setting specification"

	```sh
	s10k instructions view-service --node-id 101 \
	  --component-id net.solarnetwork.node.datum.control \
	  --service-id 1 \
	  --specification
	```

=== "Output"

	!!! note

		Note the output is always `JSON` when using `--specification`.

	```json
	[
		[
			{
				"type": "net.solarnetwork.settings.CronExpressionSettingSpecifier",
				"key": "schedule",
				"defaultValue": "0 * * * * ?",
				"descriptionArguments": [
					"https://github.com/SolarNetwork/solarnetwork/wiki/SolarNode-Cron-Job-Syntax"
				]
			},
			{
				"type": "net.solarnetwork.settings.TextFieldSettingSpecifier",
				"key": "jobService.multiDatumDataSource.uid"
			},
			{
				"type": "net.solarnetwork.settings.TextFieldSettingSpecifier",
				"key": "jobService.multiDatumDataSource.groupUid"
			},
			{
				"type": "net.solarnetwork.settings.TextFieldSettingSpecifier",
				"key": "jobService.multiDatumDataSource.controlIdRegexValue"
			},
			{
				"type": "net.solarnetwork.settings.MultiValueSettingSpecifier",
				"key": "jobService.multiDatumDataSource.eventModeValue",
				"defaultValue": "Change",
				"valueTitles": {
					"None": "Polled Only",
					"Capture": "Sampled",
					"Change": "Changed",
					"CaptureAndChange": "Sampled and changed"
				}
			},
			{
				"type": "net.solarnetwork.settings.MultiValueSettingSpecifier",
				"key": "jobService.multiDatumDataSource.persistModeValue",
				"defaultValue": "Poll",
				"valueTitles": {
					"Poll": "Poll",
					"PollAndEvent": "Poll and event"
				}
			}
		]
	]
	```

[components]: https://solarnetwork.github.io/solarnode-handbook/users/setup-app/settings/components/
[dev-specs]: https://solarnetwork.github.io/solarnode-handbook/developers/settings/specifier/
[services]: https://solarnetwork.github.io/solarnode-handbook/users/setup-app/settings/services/
[settings-csv]: https://solarnetwork.github.io/solarnode-handbook/users/settings/
