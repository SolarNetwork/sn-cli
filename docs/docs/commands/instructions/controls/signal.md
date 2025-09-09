---
title: signal
---
# Instructions Controls Signal

Send a named "signal" to a SolarNode control. Pass the desired signal name as the first (and only)
parameter.

## Usage

```
s10k instructions controls signal -node=<nodeId>
							-control=<controlId>
							[-x=<expiration>] [-X=<executionDate>]
							[-tz=<zone>] signal
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID with the control to update |
| `-control=` | `--control-id=` | the control ID to update |
| `-x=` | `--expiration=` | a date to automatically transition the instruction to `Declined` if not already completed, like `2020-10-30` or `2020-10-30T12:45` |
| `-X=` | `--exec-at=` | a date to defer instruction execution until, like `2020-10-30` or `2020-10-30T12:45` |
| `-tz=` | `--time-zone=` | a time zone ID to treat all date options as, instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |

</div>

## Parameters

Pass the signal name to send as the one and only parameter.

## Output

A status message about the result of the instruction.

## Examples

Request a [camera][cam-signal] to take a snapshot:

=== "Set component operating state"

	```sh
	s10k instructions controls signal --node-id 101 \
	  --control-id camera/1 snapshot
	```

=== "Output"

	```
	Control [camera/1] received [snapshot] signal.
	```

[cam-signal]: https://github.com/SolarNetwork/solarnetwork-node/tree/develop/net.solarnetwork.node.control.camera.ffmpeg#instruction-support
