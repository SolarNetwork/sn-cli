---
title: submit
---
# Datum Imports Submit Job

Submit a datum import job configuration.

The datum to import is specified by the `--data-file` option, which is a path to the data file to
import. Some input services, such as the Cloud Integration service, do **not** require a data file,
however, so this option does not need to be provided for those services.

The job can be staged with the `--stage` option, in which case the job will start in the `Staged`
state and not progess any further until confirmed (see the [confirm-staged](./confirm-staged.md)
command to do that). A staged job can also be previewed so you can verify the configuration (see the
[preview-staged](./preview-staged.md) command to do that).

If the job is **not** staged, it will start in the `Queued` state and will be processed by
SolarNetwork at some point in the future.

Use the [view](./view.md) or [list](./list.md) commands to monitor a job's overall status.

## Input format settings

The [input format][input-formats] settings to use can be provided by a combination of methods:

 1. Standard input, as a JSON object
 3. Command line options
 2. Command line parameter JSON object, or `@@` file reference

For example, the following invocations produce equivalent results:

```sh
# using standard input
echo '{"dateFormat":"yyyy-MM-dd HH:mm:ss","instantaneousDataColumns:"D-F"}' \
    |s10k datum imports import --stage \
		--name 'My Import Job' \
		--time-zone Pacific/Auckland \
		--service simple \
	    --data-file 'my-data.csv'

# using parameter value
s10k datum imports import --stage \
	--name 'My Import Job' \
	--time-zone Pacific/Auckland \
	--service simple \
	--data-file 'my-data.csv' \
	'{"dateFormat":"yyyy-MM-dd HH:mm:ss","instantaneousDataColumns:"D-F"}'

# using parameter file reference - my-file.json contains
# {"dateFormat":"yyyy-MM-dd HH:mm:ss","instantaneousDataColumns:"D-F"}
s10k datum imports import --stage \
	--name 'My Import Job' \
	--time-zone Pacific/Auckland \
	--service simple \
	--data-file 'my-data.csv' \
    @@my-file.json

# using options
s10k datum imports import --stage \
	--name 'My Import Job' \
	--time-zone Pacific/Auckland \
	--service simple \
	--data-file 'my-data.csv' \
	--service-property 'dateFormat:yyyy-MM-dd HH:mm:ss' \
	--service-property 'instantaneousDataColumns:D-F'
```

The methods can be combined, with each method overriding settings duplicated in previous methods.
For example the following ends up changing `instantaneousDataColumns` to `F-H` because the command
line parameter overrides both the `--service-property` option and standard input values:

```sh
echo '{"instantaneousDataColumns":"D-F"}' \
    |s10k datum imports import --stage \
		--name 'My Import Job' \
		--time-zone Pacific/Auckland \
		--service simple \
		--data-file 'my-data.csv' \
		--service-property 'instantaneousDataColumns:E-G' \
		'{"instantaneousDataColumns":"F-H"}'
```

## Usage

```
s10k datum imports submit
	[-sI]
	[-f=<dataFile>]
	[-m=<name>]
	[-b=<batchSize>]
	[-G=<groupKey>]
	[-tz=<zone>]
	-S=<serviceIdentifier>
	[-g=<mode>]
	[-prop=serviceProperty]...
	[-mode=<displayMode>]
	[<config>]
```

!!! tip

	Use the `--dry-run` [global option](../../../global-options.md) to preview the submission,
	without actually uploading anything. For example:

	```sh
	s10k --dry-run datum imports import --stage \
		--name 'My Import Job' \
		--time-zone Pacific/Auckland \
		--service simple \
	    --data-file 'my-data.csv' \
		--service-property 'dateFormat:yyyy-MM-dd HH:mm:ss' \
		--service-property 'headerRowCount:1' \
		--service-property 'nodeIdColumn:A' \
		--service-property 'sourceIdColumn:B' \
		--service-property 'dateColumnsValue:C' \
		--service-property 'instantaneousDataColumns:D-F'
	```


## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-b=` | `--batch-size` | the import batch size; for Cloud Integrations import this will be forced to `1` |
| `-f=` | `--data-file` | the datum data file to import; the format of the data is specific to the input service as specified by the `--service` option |
| `-G=` | `--group-key=` | the import group key |
| `-g=`  | `--merge-mode=` | one of `Simple`, `RecursiveObjects`, or `RecursiveObjectsAndArrays` to control the merge style of settings; see [here][merge-option] for details |
| `-I` | `--ignore-input` | ignore standard input, instead of treating that as a JSON settings object |
| `-m=` | `--name=` | the job name to set |
| `-prop=` | `--service-property` | a service property, in the form `path:value` or `@@file.json`; see [here][prop-option] for details |
| `-S=` | `--service=` | the service ID of the [input format][input-formats] to use |
| `-s` | `--staged` | upload the data but do not process any further; the configuration can then be [previewed](./preview-staged.md) or [updated](./update.md) before [confirming](./confirm-staged.md) |
| `-tz=` | `--time-zone=` | a time zone ID to interpret datum timestamps that lack zone information with, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

The updated job info.

# Examples

=== "Preview staged datum import"

	```sh
	s10k datum imports confirm-staged --job-id 49a2f730-0000-0000-0000-233d085c799a
	```

=== "Preview staged datum import (shortcut)"

	You can use `imp` instead of `imports` and `confirm` instead of `confirm-staged`:

	```sh
	s10k datum imp confirm --job-id 49a2f730-0000-0000-0000-233d085c799a
	```

=== "Pretty Output"

	```
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	| Job Name                                 | Job ID                               | Group ID                             | Submit Date              | State  | Import Date              | Success | Started At | Completed At | Loaded | % Complete | Batch Size | Input Service | Input Time Zone | Input Properties                             |
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	| 1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import | 49a2f730-0000-0000-0000-233d085c799a | 0300b452-0000-0000-0000-43e7c715601d | 2026-07-22T00:37:12.548Z | Queued | 2026-07-22T00:37:09.862Z |         |            |              |      0 | 0.0        |            | CSV - Simple  | UTC             | dateFormat               yyyy-MM-dd HH:mm:ss |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | nodeIdColumn             1                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | headerRowCount           1                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | sourceIdColumn           2                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | dateColumnsValue         3                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | accumulatingDataColumns  5                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 | instantaneousDataColumns 4                   |
	|                                          |                                      |                                      |                          |        |                          |         |            |              |        |            |            |               |                 |                                              |
	+------------------------------------------+--------------------------------------+--------------------------------------+--------------------------+--------+--------------------------+---------+------------+--------------+--------+------------+------------+---------------+-----------------+----------------------------------------------+
	```

=== "CSV Output"

	```csv
	Job Name,Job ID,Group ID,Submit Date,State,Import Date,Success,Started At,Completed At,Loaded,% Complete,Batch Size,Input Service,Input Time Zone,Input Properties
	1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import,49a2f730-0000-0000-0000-233d085c799a,0300b452-0000-0000-0000-43e7c715601d,2026-07-22T00:37:12.548Z,Queued,2026-07-22T00:37:09.862Z,,,,0,0.0,,CSV - Simple,UTC,"dateFormat               yyyy-MM-dd HH:mm:ss
	nodeIdColumn             1
	headerRowCount           1
	sourceIdColumn           2
	dateColumnsValue         3
	accumulatingDataColumns  5
	instantaneousDataColumns 4
	"
	```

=== "JSON Output"

	```json
	[
	  {
	    "userId": 857,
	    "jobId": "49a2f730-0000-0000-0000-233d085c799a",
	    "jobState": "Queued",
	    "importDate": "2026-07-22 00:37:09.862Z",
	    "groupKey": "0300b452-0000-0000-0000-43e7c715601d",
	    "success": false,
	    "submitDate": "2026-07-22 00:37:12.548Z",
	    "loadedCount": 0,
	    "percentComplete": 0.0,
	    "configuration": {
	      "name": "1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Import",
	      "stage": true,
	      "inputConfiguration": {
	        "name": "1052_%2FBLD1%2FS1%2FSY2%2FPYR%2F1_Input",
	        "serviceIdentifier": "net.solarnetwork.central.datum.imp.standard.SimpleCsvDatumImportInputFormatService",
	        "serviceProperties": {
	          "dateFormat": "yyyy-MM-dd HH:mm:ss",
	          "nodeIdColumn": "1",
	          "headerRowCount": "1",
	          "sourceIdColumn": "2",
	          "dateColumnsValue": "3",
	          "accumulatingDataColumns": "5",
	          "instantaneousDataColumns": "4"
	        },
	        "timeZoneId": "UTC"
	      }
	    }
	  }
	]
	```

[import-state]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#datum-import-task-state-type
[input-formats]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-Datum-Import-API#input-formats
[merge-option]: ../../../service-properties.md#-merge-mode-option
[prop-option]: ../../../service-properties.md#-service-property-option
