---
title: set-operating-state
---
# Instructions Controls Set-Operating-State

Update the [operating state][operating-states] of a SolarNode component. Pass the desired operating
state as the first (and only) parameter.

# Usage

```
s10k instructions controls set-operating-state -control=<controlId>
                           -node=<nodeId> desiredState
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-control=` | `--control-id=` | the control ID to set the operating state on |
| `-node=` | `--node-id=` | the node ID with the control to update |

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
