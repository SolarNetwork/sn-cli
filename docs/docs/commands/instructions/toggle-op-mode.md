---
title: toggle-op-mode
---
# Instructions Toggle-Op-Mode

Enable or disable [operating modes][op-modes] on a SolarNode. Pass one or more modes as parameters.

!!! info

	For more details on the SolarNetwork APIs used by this command, see the documentation for the
	[EnableOperationalModes][enable-op-modes] and [DisableOperationalModes][disable-op-modes]
	instruction topics.

## Usage

```
s10k instructions toggle-op-mode [-d] -node=<nodeId>
								[--mode-expiration=<modeExpiration>]
								[-x=<expiration>] [-X=<executionDate>]
								[-tz=<zone>] mode...
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-d` | `--disable` | disable the given operating modes, intead of enabling |
| `-node=` | `--node-id=` | the node ID with the control to update |
|  | `--mode-expiration=` | when **enabling** operating modes, a date to automatically disable the mode at, like `2020-10-30` or `2020-10-30T12:45` |
| `-x=` | `--expiration=` | a date to automatically transition the instruction to `Declined` if not already completed, like `2020-10-30` or `2020-10-30T12:45` |
| `-X=` | `--exec-at=` | a date to defer instruction execution until, like `2020-10-30` or `2020-10-30T12:45` |
| `-tz=` | `--time-zone=` | a time zone ID to treat all date options as, instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |

</div>

## Output

A status message about the result of the instruction.

## Examples

Enable the "hyper" operating mode:

=== "Enable `hyper` operating mode"

	```sh
	s10k instructions toggle-op-mode --node-id 101 hyper
	```

=== "Output"

	```
	Enabled operational modes [hyper].
	```

Disable the "hyper" operating mode:

=== "Disable `hyper` operating mode"

	```sh
	s10k instructions toggle-op-mode --node-id 101 --disable hyper
	```

=== "Output"

	```
	Disabled operational modes [hyper].
	```

[disable-op-modes]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#disableoperationalmodes
[enable-op-modes]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#enableoperationalmodes
[op-modes]: https://solarnetwork.github.io/solarnode-handbook/users/op-modes/
