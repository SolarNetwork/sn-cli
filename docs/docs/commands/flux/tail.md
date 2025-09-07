---
title: tail
---
# Flux Tail

Subscribe to a SolarFlux topic and display received messages.

!!! bug

	This command might print some scary looking `WARNING` messages to standard error when it starts
	up. These can be ignored by redirecting standard error, for example `2>/dev/null` in macOS or
	Linux.

## Usage

```
s10k flux tail [-G] [-prop=propName[,propName...]]...
				[-R=<maxPrecision>] [--client-id=<clientIdSuffix>]
				(
					-t=filter
					[[-node=<nodeId>] [-source=<sourceId>]]
				)
				[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-G` | `--csv-global-header` | display just one CSV header row, based either on the `-prop` values or the first message properties |
| `-prop=` | `--property=` | restrict the results to this property (or properties for multiple) |
| `-R=` | `--max-precision=` |  maximum number of decimal digits to display, or `-1` for no rounding; defaults to `3` |
|  | `--client-id` | a specific client ID to use, instead of a random default |
| `-t=` | `--topic=` | the MQTT topic filter to subscribe to (exclusive to `-node` and `-source`) |
| `-node=` | `--node-id=` | the node ID (or `+` wildcard) to show datum for (exclusive to `-topic`, requires `-source`) |
| `-source=` | `--source=` | the source ID topic pattern to show datum for (exclusive to `-topic`, requires `-node`) |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY`; **note** that `PRETTY` is not suitable for large result sets |

</div>

## Output

All matching messages. To stop receiving messages type ++ctrl+c++ on macOS or Linux.

## Examples

Subscribe to all node `101` datum sources starting with `con/`, showing just the `created`,
`sourceId`, `watts`, and `wattHours` properties. **Note** the `--csv-global-header` only
applies to the `CSV` output:


=== "Tail datum showing just _watts_ and _wattHours_"

	```sh
	s10k flux tail --node-id 101 --source-id con/\#  \
	  --csv-global-header \
	  --property created,sourceId,watts,wattHours
	```

=== "Pretty Output"

	```
	+--------------------------+----------+-------+-----------+
	| created                  | sourceId | watts | wattHours |
	+--------------------------+----------+-------+-----------+
	| 2025-09-05T06:24:55.227Z | con/pcm  |  2408 |   8259102 |
	+--------------------------+----------+-------+-----------+
	+----------------------+----------+-------+-----------+
	| created              | sourceId | watts | wattHours |
	+----------------------+----------+-------+-----------+
	| 2025-09-05T06:26:35Z | con/1    |  1252 |   8258876 |
	+----------------------+----------+-------+-----------+
	```

=== "CSV Output"


	```csv
	created,sourceId,watts,wattHours
	2025-09-05T06:39:35Z,con/1,3087,8259410
	2025-09-05T06:39:55.227Z,con/pcm,3270,8259700
	```

=== "JSON Output"

	```json
	{
		"ts" : "2025-09-05 06:40:25.732246Z",
		"body" : {
			"created" : "2025-09-05 06:40:25.227Z",
			"sourceId" : "con/pcm",
			"watts" : "2653",
			"wattHours" : "8259722"
		},
		"topic" : "user/123/node/101/datum/0/con/pcm"
	}
	{
		"ts" : "2025-09-05 06:40:35.511049Z",
		"body" : {
			"created" : "2025-09-05 06:40:35Z",
			"sourceId" : "con/1",
			"watts" : "3363",
			"wattHours" : "8259466"
		},
		"topic" : "user/123/node/101/datum/0/con/1"
	}
	```

Monitor OCPP inbound message events for charger:

=== "Tail OCPP charger events"

	```sh
	s10k flux tail --display-mode JSON \
	  --topic user/123/event/ocpp/message/received \
	  |jq --unbuffered 'select(.body.data.cp == "chgr123") | .body.data'
	```

=== "JSON Output"

	```json
	{
		"cp": "chgr123",
		"messageId": "3ed43c9f-7fc2-4ed8-ac51-70e1987e8e80",
		"action": "MeterValues",
		"message": {
			"connectorId": 2,
			"transactionId": 257979,
			"meterValue": [
				{
					"timestamp": "2025-09-07T04:51:43.000+00:00",
					"sampledValue": [
					{
						"value": "289314130",
						"context": "Sample.Periodic",
						"format": "Raw",
						"measurand": "Energy.Active.Import.Register",
						"location": "Outlet",
						"unit": "Wh"
					}
					{
						"value": "3498.0",
						"context": "Sample.Periodic",
						"measurand": "Power.Active.Import",
						"location": "Outlet",
						"unit": "W"
					}
					]
				}
			]
		}
	}
	{
		"cp": "chgr123",
		"messageId": "d8551a59-4cf5-4958-8487-ca2dedfcb21a",
		"action": "Heartbeat"
	}
	```
