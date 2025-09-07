---
title: set
---
# Instructions Controls Set

Update the value of a SolarNode control. Pass the desired control value as the first (and only)
parameter.

## Usage

```
s10k instructions controls set -node=<nodeId> -control=<controlId>
							[-x=<expiration>] [-X=<executionDate>]
							[-tz=<zone>] desiredValue
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

Update a switch-type boolean control to "on" (using `1` to represent "on"):

=== "Set boolean control to 'on'"

	```sh
	s10k instructions controls set --node-id 101 \
	  --control-id switch/1 1
	```

=== "Output"

	```
	Control [switch/1] set to [1]
	```
