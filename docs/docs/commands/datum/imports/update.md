---
title: update
---
# Datum Imports Update

Update the configuration of a datum import job. This is primarily useful for changing the
configuration of a staged job. For more details on datum import, see the [API
documentation][datum-import-api].

!!! note

	Note the JSON configuration passed on standard input or as the command parameter will be applied
	as _service properties_ on the _input configuration_ portion of the overall import job
	configuration. Specifically, it will apply to the
	`configuration.inputConfiguration.serviceProperties` property. To modify other aspects of the
	configuration, use the various options provided by this command, for example `--name` to change
	the job name.

## Usage

```
s10k datum imports update
	[-I]
	-j=<jobId>
	[-m=<name>]
	[-b=<batchSize>]
	[-G=<groupKey>]
	[-tz=<zone>]
	[-S=<serviceIdentifier>]
	[-g=<mode>]
	[-prop=serviceProperty]...
	[-mode=<displayMode>]
	[<config>]
```

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to preview the update,
	without actually changing anything. For example:

	```sh
	s10k --dry-run datum imports update --job-id 49a2f730-0000-0000-0000-233d085c799a --time-zone UTC
	```


## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-b=` | `--batch-size` | the import batch size; for Cloud Integrations import this will be forced to `1` |
| `-G=` | `--group-key=` | the import group key |
| `-g=`  | `--merge-mode=` | one of `Simple`, `RecursiveObjects`, or `RecursiveObjectsAndArrays` to control the merge style of settings; see [here][merge-option] for details |
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON settings object |
| `-j=` | `--job-id=` | the ID of the job to update |
| `-m=` | `--name=` | the job name to set |
| `-prop=` | `--service-property` | a service property, in the form `path:value` or `@@file.json`; see [here][prop-option] for details |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

The updated job info (or a preview of the update if the `--dry-run` option was given).

# Examples

=== "Preview changes"

	Here we will update the job name and time zone, as well as change the `dateFormat`
	service property:

	```sh
	s10k datum imports update --job-id 49a2f730-0000-0000-0000-233d085c799a \
	    --name 'New Job Name'
		--time-zone Pacific/Auckland
		--service-property 'dateFormat:yyyy-MM-dd HH:mm'
	```

=== "Preview staged datum import (shortcut)"

	You can use `imp` instead of `imports` and `confirm` instead of `confirm-staged`:

	```sh
	s10k datum imp update --job-id 49a2f730-0000-0000-0000-233d085c799a \
	    --name 'New Job Name'
		--time-zone Pacific/Auckland
		--service-property 'dateFormat:yyyy-MM-dd HH:mm'
	```

=== "Pretty Output"

	```
	+--------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+------------------+-------------------------------------------+
	| Job Name     | Job ID                               | Group ID                             | Submit Date              | State  | Import Date              | Success | Started At | Completed At | Loaded | % Complete | Batch Size | Input Service | Input Time Zone  | Input Properties                          |
	+--------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+------------------+-------------------------------------------+
	| New Job Name | 7b575e23-fc59-41a7-a53d-517068f08ac0 | b44fe586-a483-4b31-ab12-b176e18e5aea | 2025-10-21T02:49:30.098Z | Staged | 2025-10-21T02:49:28.865Z |         |            |              |      0 |          0 |      10000 | CSV - Simple  | Pacific/Auckland | dateFormat               yyyy-MM-dd HH:mm |
	|              |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                  | nodeIdColumn             1                |
	|              |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                  | headerRowCount           1                |
	|              |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                  | sourceIdColumn           2                |
	|              |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                  | tagDataColumns                            |
	|              |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                  | dateColumnsValue         3                |
	|              |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                  | statusDataColumns                         |
	|              |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                  | accumulatingDataColumns  5                |
	|              |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                  | instantaneousDataColumns 4                |
	|              |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                  |                                           |
	+--------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+------------------+-------------------------------------------+
	```

=== "CSV Output"

	```csv
	Job Name,Job ID,Group ID,Submit Date,State,Import Date,Success,Started At,Completed At,Loaded,% Complete,Batch Size,Input Service,Input Time Zone,Input Properties
	New Job Name,7b575e23-fc59-41a7-a53d-517068f08ac0,b44fe586-a483-4b31-ab12-b176e18e5aea,2025-10-21T02:49:30.098Z,Staged,2025-10-21T02:49:28.865Z,,,,0,0,10000,CSV - Simple,Pacific/Auckland,"dateFormat               yyyy-MM-dd HH:mm
	nodeIdColumn             1
	headerRowCount           1
	sourceIdColumn           2
	tagDataColumns
	dateColumnsValue         3
	statusDataColumns
	accumulatingDataColumns  5
	instantaneousDataColumns 4
	"
	```

=== "JSON Output"

	```json
	[
	  {
	    "userId": 147,
	    "jobId": "7b575e23-fc59-41a7-a53d-517068f08ac0",
	    "jobState": "Staged",
	    "importDate": "2025-10-21 02:49:28.865Z",
	    "groupKey": "b44fe586-a483-4b31-ab12-b176e18e5aea",
	    "success": false,
	    "submitDate": "2025-10-21 02:49:30.098Z",
	    "loadedCount": 0,
	    "percentComplete": 0.0,
	    "configuration": {
	      "name": "New Job Name",
	      "stage": true,
	      "batchSize": 10000,
	      "inputConfiguration": {
	        "name": "New Job Name",
	        "serviceIdentifier": "net.solarnetwork.central.datum.imp.standard.SimpleCsvDatumImportInputFormatService",
	        "serviceProperties": {
	          "dateFormat": "yyyy-MM-dd HH:mm",
	          "nodeIdColumn": "1",
	          "headerRowCount": "1",
	          "sourceIdColumn": "2",
	          "tagDataColumns": "",
	          "dateColumnsValue": "3",
	          "statusDataColumns": "",
	          "accumulatingDataColumns": "5",
	          "instantaneousDataColumns": "4"
	        },
	        "timeZoneId": "Pacific/Auckland"
	      }
	    }
	  }
	]
	```

[import-state]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#datum-import-task-state-type
[datum-import-api]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Datum-Import-API
