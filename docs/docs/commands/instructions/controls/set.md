---
title: set
---
# Instructions Controls Set

Update the value of a SolarNode control. Pass the desired control value as the first (and only)
parameter.

# Usage

```
s10k instructions controls set -control=<controlId> -node=<nodeId> desiredValue
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-control=` | `--control-id=` | the control ID to update |
| `-node=` | `--node-id=` | the node ID with the control to update |

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
