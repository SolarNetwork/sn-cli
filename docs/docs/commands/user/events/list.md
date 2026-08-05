---
title: list
---
# User Events List

List user event information matching search criteria.

## Usage

```
s10k user events
	[-EDq]
	[-F=<searchFilter>]
	[-t=tag[,tag...]]...
    -min=<minDate> -max=<maxDate> [-tz=<zone>]
    [-c=columReference[,columReference...]]...
    [-x=columExpression[,columExpression...]]...
	[-M=max] [-O=<resultOffset>]
	[-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-c=` | `--column=` | a name:path reference to extract from the event data into a column in tabular output modes; see [Column references](#column-references) for details |
| `-D` | `--show-data` | show event data in the tabular output modes when `--column` options are given |
| `-E` | `--show-event-id` | show event IDs in the tabular output modes |
| `-F=` | `--search-filter=` | an event data search filter to match; see [Search filters](#search-filters) for details |
| `-M=` | `--max=` | the maximum number of records to return |
| `-min=` | `--min-date=` | a minimum date to limit results to, like `2020-10-30` or `2020-10-30T12:45` |
| `-max=` | `--max-date=` | a maximum date (exclusive) to limit results to, in same form as `-min` |
| `-O=` | `--offset=` | start returning records from this offset, `0` being the first record |
| `-q` | `--quiet` | suppress expression evaluation errors |
| `-t=` | `--tag=` | a tag to match; multiple tags match using a logical **AND** |
| `-tz=` | `--time-zone=` | a time zone ID to match, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-x=` | `--expression=` | a name:expression reference to extract from the event data as a column in tabular output mode |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

### Column references

The `--column` option lets you extract specific values out of the event data into columns in `PRETTY` and `CSV` display modes.
When this option is given, then by default the **Data** column is not shown. You can force the data column to be included still
by providing the `--show-data` option.

Each column reference value has a `name:path` syntax, where `name` is the desired column name and
`path` is a URL path-like reference to the data value to extract. The leading `/` of the path is
optional. The name can include a `>` suffix to make the values right-aligned in `PRETTY` mode.

For example, imagine this event generated when a Cloud Integration Rake Task began execution:

```json title="Example user event"
{
  "userId" : 123,
  "eventId" : "019fcb57-bea3-7ac6-b972-6af8743af668",
  "created" : "2026-08-04 05:55:53.123689Z",
  "tags" :   [ "c2c", "ds", "rake" ],
  "message" : "Rake for datum",
  "data" : {
    "subId" : 345,
    "configId" : 234,
    "executeAt" : "2026-08-04 04:00:00Z",
    "startedAt" : "2026-08-04 05:55:53.119971523Z",
    "dateOffset" : "P14D"
  }
}
```

If you were interested in the `configId`, `dateOffset`, and `executeAt` data values, you could extract them into columns
with options like:

=== "Extract columns"

	```sh
	s10k user events list ...  \
	    --column 'Config ID>:configId' \
		--column 'Offset>:dateOffset' \
		--column 'Rake Date:executeAt'
	```

=== "Pretty Output"

	```
	+----------------------------------+----------------------+------------------+-----------+--------+----------------------+
	| Event Date                       | Tags                 | Message          | Config ID | Offset | Rake Date            |
	+----------------------------------+----------------------+------------------+-----------+--------+----------------------+
	| 2026-08-04 17:55:53.123689+12:00 | c2c,ds,rake          | Rake for datum   |       234 |   P14D | 2026-08-04 04:00:00Z |
	+----------------------------------+----------------------+------------------+-----------+--------+----------------------+
	```

	!!! tip

		Note how the **Config ID** and **Offset** columns are right-aligned, because of the `>` suffix added to those
		column names in the `--column` options.

=== "CSV Output"

	```csv
	Event Date,Tags,Message,Config ID,Offset,Rake Date
	2026-08-04 17:55:53.123689+12:00,"c2c,ds,rake",Rake for datum,234,P14D,2026-08-04 04:00:00Z
	```

### Expression columns

The `--expression` option can be used to extract specific values out of events into columns in
`PRETTY` and `CSV` output modes, using [SpEL][spel] expressions. The event itself is used for the
expression root object, meaning you can access any of the properties available on the event.

Take this OCPP inbound message event as an example:

```json title="Example OCPP inbound message event"
{
  "userId" : 123,
  "eventId" : "019fc630-9ae7-7c67-b110-7b27c85a07cc",
  "created" : "2026-08-03 05:55:01.991793Z",
  "tags" :   [ "ocpp", "message", "received" ],
  "data" : {
    "cp" : "chgr03",
    "action" : "MeterValues",
    "message" : {
      "meterValue" :   [ {
        "timestamp" : "2026-08-03T05:54:59.000Z",
        "sampledValue" : [ {
          "unit" : "W",
          "value" : "0",
          "location" : "Outlet",
          "measurand" : "Power.Active.Import"
        }, {
          "unit" : "Percent",
          "value" : "60",
          "location" : "EV",
          "measurand" : "SoC"
        } ]
      } ],
      "connectorId" : 1,
      "transactionId" : 605279
    },
    "messageId" : "cc5b6ab1-49be-4f62-815b-3a378c2de2dd"
  }
}
```

If you were interested in the `cp` and `SoC` measurand data values, you could extract them into columns
with options like:

=== "Extract expression"

	```sh
	s10k user events list ...  \
	    --column 'Charger:cp' \
		--column "SoC>:data.action == 'MeterValues' ? data.message.meterValue[0].sampledValue.^[measurand == 'SoC']?.value : null"
	```

=== "Pretty Output"

	```
	+----------------------------------+-----------------------+---------+---------+-----+
	| Event Date                       | Tags                  | Message | Charger | SoC |
	+----------------------------------+-----------------------+---------+---------+-----+
	| 2026-08-03 17:55:01.991793+12:00 | ocpp,message,received |         | chgr03  |  60 |
	+----------------------------------+-----------------------+---------+---------+-----+
	```

=== "CSV Output"

	```csv
	Event Date,Tags,Message,Charger,SoC
	2026-08-03 17:55:01.991793+12:00,"ocpp,message,received",,chgr03,60
	```


### Search filters

The `--search-filter` option allows you to filter the events based on values in the data. It uses an LDAP-like
filter syntax.

TODO

## Output

A listing of all available nodes. Each record contains the following properties:

| Property | Description |
|:---------|:------------|
| **User ID** | The account owner ID. Not shown in tabular output modes. |
| **Event ID** | The event ID. Not shown in tabular output modes unless the `--show-event-id` option is given. |
| **Event Date** | The date the event was created. |
| **Tags** | A list of tags that categorize the event. In `JSON` output mode this is the `created` property. |
| **Message** | The display message. |
| **Data** | The map-like event data. Not shown in tabular output modes when `--column` options are given, unless the `--show-data` option is also given. |

## Examples

=== "List nodes"

	```sh
	s10k user events list --min-date X --max-date Y
	```

=== "Pretty Output"

	```

	```

=== "CSV Output"

	```csv

	```

=== "JSON Output"

	```json

	```

[spel]: https://github.com/SolarNetwork/solarnetwork/wiki/Spring-Expression-Language-Syntax
