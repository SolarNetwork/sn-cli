---
title: view
---
# Location Requests View

View a location request.

## Usage

```
s10k locations requests view
	-r=<requestId>
	[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-r=` | `--request-id` | the ID of the request to view |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

The location request information.

## Examples

=== "View request"

	```sh
	s10k locations requests view --request-id 3
	```

=== "List requests (shortcut)"

	You can use `locs` instead of `locations` and `reqs` instead of `requests`:

	```sh
	s10k locs reqs view --request-id 3
	```

=== "Pretty Output"

	```
	╔════╤══════════════════════════════════╤══════════════════════════════════╤═══════════╤═════════════╤════════════════╤════════════════════════╤═══════════════════════════╤═════════╗
	║ ID │ Created                          │ Modified                         │ Status    │ Location ID │ Source ID      │ Features               │ Info                      │ Message ║
	╠════╪══════════════════════════════════╪══════════════════════════════════╪═══════════╪═════════════╪════════════════╪════════════════════════╪═══════════════════════════╪═════════╣
	║  3 │ 2026-08-30 17:30:10.387888+12:00 │ 2026-08-30 17:30:10.387888+12:00 │ Submitted │             │ OpenWeatherMap │ weather, forecast, day │ name     Wellington       │         ║
	║    │                                  │                                  │           │             │                │                        │ zone     Pacific/Auckland │         ║
	║    │                                  │                                  │           │             │                │                        │ region   Wellington       │         ║
	║    │                                  │                                  │           │             │                │                        │ country  NZ               │         ║
	║    │                                  │                                  │           │             │                │                        │ locality Wellington       │         ║
	║    │                                  │                                  │           │             │                │                        │                           │         ║
	╚════╧══════════════════════════════════╧══════════════════════════════════╧═══════════╧═════════════╧════════════════╧════════════════════════╧═══════════════════════════╧═════════╝
	```

=== "CSV Output"

	```csv
ID,Created,Modified,Status,Location ID,Source ID,Features,Info,Message
3,2026-08-30 17:30:10.387888+12:00,2026-08-30 17:30:10.387888+12:00,Submitted,,OpenWeatherMap,"weather, forecast, day","name     Wellington
zone     Pacific/Auckland
region   Wellington
country  NZ
locality Wellington
",
	```

=== "JSON Output"

	```json
	{
	  "id": 3,
	  "created": "2026-08-30 05:30:10.387888Z",
	  "modified": "2026-08-30 05:30:10.387888Z",
	  "userId": 147,
	  "status": "Submitted",
	  "data": {
	    "features": [
	      "weather",
	      "forecast",
	      "day"
	    ],
	    "location": {
	      "name": "Wellington",
	      "zone": "Pacific/Auckland",
	      "region": "Wellington",
	      "country": "NZ",
	      "locality": "Wellington"
	    },
	    "sourceId": "OpenWeatherMap"
	  }
	}
	```
