---
title: list-services
---
# Instructions List-Services

List the available [services][services] on a node.

## Usage

```
s10k instructions list-services -node=<nodeId> [-mode=<displayMode>]
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to list the control IDs for |
| `-mode=` | `--display-mode=` | the format to display the data as, one of `CSV`, `JSON`, or `PRETTY`; defaults to `PRETTY` |

</div>

## Output

A listing of available service records.

## Examples

You can list all available services on a node like this:

=== "Show available services"

	```sh
	s10k instructions list-services --node-id 101
	```

=== "Pretty output"

	```
	+---------------------------------------------------------------------------+------------------------------------+
	| ID                                                                        | Title                              |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.backup.DefaultBackupManager                         | Backup Manager                     |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.backup.s3.S3BackupService                           | S3 Backup Service                  |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.control.modbus.csv                                  | Modbus Control CSV Configurer      |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.dao.jdbc.general.datum                              | Datum Persistence (JDBC)           |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.dao.jdbc.h2.backup                                  | Database Backup                    |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.dao.jdbc.locstate                                   | Local State Persistence (JDBC)     |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.dao.jdbc.trim                                       | General Datum Jobs                 |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.datum.modbus.csv                                    | Modbus Device CSV Configurer       |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.datum.xform.solarin                                 | Global Datum Filter Chain          |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.job.BackupJob                                       | Core Jobs                          |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.location.ws                                         | Location Service (SolarNet)        |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.metadata.json                                       | Node Metadata Service (SolarNet)   |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.metadata.json.JsonDatumMetadataService              | Source Metadata Service (SolarNet) |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.reactor.simple                                      | Reactor Jobs                       |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.runtime.DefaultOperationalModesService              | Operational Modes Service          |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.runtime.dq                                          | Datum Queue                        |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.settings.ca.backup                                  | Settings Local Backup              |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.system.cmdline.CmdlineSystemService                 | System Service                     |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.system.ssh.RemoteSshService                         | Remote SSH Service                 |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.upload.bulkjsonwebpost                              | SolarIn/HTTP Upload Service Jobs   |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.upload.bulkjsonwebpost.BulkJsonWebPostUploadService | SolarIn/HTTP Upload Service        |
	+---------------------------------------------------------------------------+------------------------------------+
	| net.solarnetwork.node.upload.mqtt                                         | SolarIn/MQTT Integration           |
	+---------------------------------------------------------------------------+------------------------------------+
	```

=== "CSV output"

	```csv
	ID,Title
	net.solarnetwork.node.backup.DefaultBackupManager,Backup Manager
	net.solarnetwork.node.backup.s3.S3BackupService,S3 Backup Service
	net.solarnetwork.node.control.modbus.csv,Modbus Control CSV Configurer
	net.solarnetwork.node.dao.jdbc.general.datum,Datum Persistence (JDBC)
	net.solarnetwork.node.dao.jdbc.h2.backup,Database Backup
	net.solarnetwork.node.dao.jdbc.locstate,Local State Persistence (JDBC)
	net.solarnetwork.node.dao.jdbc.trim,General Datum Jobs
	net.solarnetwork.node.datum.modbus.csv,Modbus Device CSV Configurer
	net.solarnetwork.node.datum.xform.solarin,Global Datum Filter Chain
	net.solarnetwork.node.job.BackupJob,Core Jobs
	net.solarnetwork.node.location.ws,Location Service (SolarNet)
	net.solarnetwork.node.metadata.json,Node Metadata Service (SolarNet)
	net.solarnetwork.node.metadata.json.JsonDatumMetadataService,Source Metadata Service (SolarNet)
	net.solarnetwork.node.reactor.simple,Reactor Jobs
	net.solarnetwork.node.runtime.DefaultOperationalModesService,Operational Modes Service
	net.solarnetwork.node.runtime.dq,Datum Queue
	net.solarnetwork.node.settings.ca.backup,Settings Local Backup
	net.solarnetwork.node.system.cmdline.CmdlineSystemService,System Service
	net.solarnetwork.node.system.ssh.RemoteSshService,Remote SSH Service
	net.solarnetwork.node.upload.bulkjsonwebpost,SolarIn/HTTP Upload Service Jobs
	net.solarnetwork.node.upload.bulkjsonwebpost.BulkJsonWebPostUploadService,SolarIn/HTTP Upload Service
	net.solarnetwork.node.upload.mqtt,SolarIn/MQTT Integration
	```

=== "JSON output"

	```json
	[
		{
			"id": "net.solarnetwork.node.backup.DefaultBackupManager",
			"title": "Backup Manager"
		},
		{
			"id": "net.solarnetwork.node.backup.s3.S3BackupService",
			"title": "S3 Backup Service"
		},
		{
			"id": "net.solarnetwork.node.control.modbus.csv",
			"title": "Modbus Control CSV Configurer"
		},
		{
			"id": "net.solarnetwork.node.dao.jdbc.general.datum",
			"title": "Datum Persistence (JDBC)"
		},
		{
			"id": "net.solarnetwork.node.dao.jdbc.h2.backup",
			"title": "Database Backup"
		},
		{
			"id": "net.solarnetwork.node.dao.jdbc.locstate",
			"title": "Local State Persistence (JDBC)"
		},
		{
			"id": "net.solarnetwork.node.dao.jdbc.trim",
			"title": "General Datum Jobs"
		},
		{
			"id": "net.solarnetwork.node.datum.modbus.csv",
			"title": "Modbus Device CSV Configurer"
		},
		{
			"id": "net.solarnetwork.node.datum.xform.solarin",
			"title": "Global Datum Filter Chain"
		},
		{
			"id": "net.solarnetwork.node.job.BackupJob",
			"title": "Core Jobs"
		},
		{
			"id": "net.solarnetwork.node.location.ws",
			"title": "Location Service (SolarNet)"
		},
		{
			"id": "net.solarnetwork.node.metadata.json",
			"title": "Node Metadata Service (SolarNet)"
		},
		{
			"id": "net.solarnetwork.node.metadata.json.JsonDatumMetadataService",
			"title": "Source Metadata Service (SolarNet)"
		},
		{
			"id": "net.solarnetwork.node.reactor.simple",
			"title": "Reactor Jobs"
		},
		{
			"id": "net.solarnetwork.node.runtime.DefaultOperationalModesService",
			"title": "Operational Modes Service"
		},
		{
			"id": "net.solarnetwork.node.runtime.dq",
			"title": "Datum Queue"
		},
		{
			"id": "net.solarnetwork.node.settings.ca.backup",
			"title": "Settings Local Backup"
		},
		{
			"id": "net.solarnetwork.node.system.cmdline.CmdlineSystemService",
			"title": "System Service"
		},
		{
			"id": "net.solarnetwork.node.system.ssh.RemoteSshService",
			"title": "Remote SSH Service"
		},
		{
			"id": "net.solarnetwork.node.upload.bulkjsonwebpost",
			"title": "SolarIn/HTTP Upload Service Jobs"
		},
		{
			"id": "net.solarnetwork.node.upload.bulkjsonwebpost.BulkJsonWebPostUploadService",
			"title": "SolarIn/HTTP Upload Service"
		},
		{
			"id": "net.solarnetwork.node.upload.mqtt",
			"title": "SolarIn/MQTT Integration"
		}
	]
	```

[services]: https://solarnetwork.github.io/solarnode-handbook/users/setup-app/settings/services/
