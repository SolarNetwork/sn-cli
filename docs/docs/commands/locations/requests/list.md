---
title: list
---
# Location Requests List

List location requests matching search criteria.

## Usage

```
s10k locations requests list
	[-s=status[,status...]]...
	[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-s=` | `--status` | a status to match, one of `Submitted`, `Rejected`, `Duplicate`, `Created` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of matching location requests. Each record contains the following properties:

| Property | Description |
|:---------|:------------|
| **ID** | A unique identifier for the request. |
| **Created** | The creation date. |
| **Modified** | The modification date. |
| **Status** | The request status, one of `Submitted`, `Rejected`, `Duplicate`, or `Created`. |
| **Location ID** | The assigned location ID. |
| **Source ID** | The source ID. |
| **Features** | The requested features. |
| **Info** | The requested location information. |
| **Message** | An optional message. |

## Examples

=== "List locations"

	```sh
	s10k locations requests list --status Completed
	```

=== "List locations (shortcut)"

	You can use `locs` instead of `locations` and `reqs` instead of `requests`:

	```sh
	s10k locs reqs list --status Completed
	```

=== "Pretty Output"

	```
	╔════╤══════════════════════════════════╤══════════════════════════════════╤═════════╤═════════════╤════════════════╤════════════════════════╤═════════════════════════════════════╤═════════╗
	║ ID │ Created                          │ Modified                         │ Status  │ Location ID │ Source ID      │ Features               │ Info                                │ Message ║
	╠════╪══════════════════════════════════╪══════════════════════════════════╪═════════╪═════════════╪════════════════╪════════════════════════╪═════════════════════════════════════╪═════════╣
	║ 45 │ 2026-08-28 20:47:42.468111+12:00 │ 2026-08-29 12:39:28.029832+12:00 │ Created │ 1           │ OpenWeatherMap │ weather, forecast, day │ name            Cupertino           │         ║
	║    │                                  │                                  │         │             │                │                        │ zone            America/Los_Angeles │         ║
	║    │                                  │                                  │         │             │                │                        │ country         US                  │         ║
	║    │                                  │                                  │         │             │                │                        │ locality        Cupertino           │         ║
	║    │                                  │                                  │         │             │                │                        │ stateOrProvince California          │         ║
	║    │                                  │                                  │         │             │                │                        │                                     │         ║
	╟────┼──────────────────────────────────┼──────────────────────────────────┼─────────┼─────────────┼────────────────┼────────────────────────┼─────────────────────────────────────┼─────────╢
	║ 46 │ 2026-08-28 20:48:49.495883+12:00 │ 2026-08-29 12:39:38.62693+12:00  │ Created │ 2           │ OpenWeatherMap │ weather, forecast, day │ name            Florence            │         ║
	║    │                                  │                                  │         │             │                │                        │ zone            America/Phoenix     │         ║
	║    │                                  │                                  │         │             │                │                        │ country         US                  │         ║
	║    │                                  │                                  │         │             │                │                        │ locality        Florence            │         ║
	║    │                                  │                                  │         │             │                │                        │ stateOrProvince Arizona             │         ║
	║    │                                  │                                  │         │             │                │                        │                                     │         ║
	╚════╧══════════════════════════════════╧══════════════════════════════════╧═════════╧═════════════╧════════════════╧════════════════════════╧═════════════════════════════════════╧═════════╝
	```

=== "CSV Output"

	```csv
	ID,Created,Modified,Status,Location ID,Source ID,Features,Info,Message
	45,2026-08-28 20:47:42.468111+12:00,2026-08-29 12:39:28.029832+12:00,Created,1,OpenWeatherMap,"weather, forecast, day","name            Cupertino
	zone            America/Los_Angeles
	country         US
	locality        Cupertino
	stateOrProvince California
	",
	46,2026-08-28 20:48:49.495883+12:00,2026-08-29 12:39:38.62693+12:00,Created,2,OpenWeatherMap,"weather, forecast, day","name            Florence
	zone            America/Phoenix
	country         US
	locality        Florence
	stateOrProvince Arizona
	"
	```

=== "JSON Output"

	```json
	[
	  {
	    "id": 45,
	    "created": "2026-08-28 08:47:42.468111Z",
	    "modified": "2026-08-29 00:39:28.029832Z",
	    "userId": 147,
	    "status": "Created",
	    "locationId": 1,
	    "data": {
	      "features": [
	        "weather",
	        "forecast",
	        "day"
	      ],
	      "location": {
	        "name": "Cupertino",
	        "zone": "America/Los_Angeles",
	        "country": "US",
	        "locality": "Cupertino",
	        "stateOrProvince": "California"
	      },
	      "sourceId": "OpenWeatherMap"
	    }
	  },
	  {
	    "id": 46,
	    "created": "2026-08-28 08:48:49.495883Z",
	    "modified": "2026-08-29 00:39:38.62693Z",
	    "userId": 147,
	    "status": "Created",
	    "locationId": 2,
	    "data": {
	      "features": [
	        "weather",
	        "forecast",
	        "day"
	      ],
	      "location": {
	        "name": "Florence",
	        "zone": "America/Phoenix",
	        "country": "US",
	        "locality": "Florence",
	        "stateOrProvince": "Arizona"
	      },
	      "sourceId": "OpenWeatherMap"
	    }
	  }
	]
	```
