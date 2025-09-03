---
title: set-operating-state
---
# Instructions Controls Set-Operating-State

Update the [operating state][operating-states] of a SolarNode component. Pass the desired operating
state as the first (and only) parameter.

## Usage

```
s10k instructions controls set-operating-state -node=<nodeId>
							-control=<controlId>
							[-x=<expiration>] [-X=<executionDate>]
							[-tz=<zone>] desiredState
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

## Output

A status message about the result of the instruction.

## Examples

Update a component to "standby" mode:

=== "Set component operating state"

	```sh
	s10k instructions controls set-operating-state --node-id 101 \
	  --control-id pump/1 Standby
	```

=== "Output"

	```
	Control [pump/1] operating state set to [Standby]
	```

[operating-states]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-global-objects#standard-device-operating-states
