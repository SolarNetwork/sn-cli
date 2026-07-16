---
title: orchestrate
---
# Instructions Controls Orchestrate

Schedule a set of control changes.

!!! tip

	See the [`OrchestrateControls`][OrchestrateControls] instruction documentation for more information.

## Usage

```
s10k instructions controls orchestrate
	-node=<nodeId>
	-control=<controlId>
	-X=<executionDate>
	[-param=parameter[,parameter...]]...
	[-x=<expiration>]
	[-tz=<zone>]
```

!!! note

	The `--control-id` option refers to the **Service Name** of the component you want to handel the
	orchestration instruction. Typically this is a **Control Conductor** component.

	Additionally, the `--exec-at` option is mandatory and is used as the **orchestration date** for
	the instruction, instead of the more general `executionDate` instruction parameter.

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID with the control schedule to execute |
| `-control=` | `--control-id=` | the **Service Name** of the component with the schedule to execute |
| `-param=` | `--parameter=` | an extra instruction parameter to pass to the schedule, in the form `name:value` |
| `-tz=` | `--time-zone=` | a time zone ID to treat all date options as, instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |
| `-x=` | `--expiration=` | a date to automatically transition the instruction to `Declined` if not already completed, like `2020-10-30` or `2020-10-30T12:45` |
| `-X=` | `--exec-at=` | the orchestration date to execute the control schedule at, like `2020-10-30` or `2020-10-30T12:45` |

</div>

## Output

A status message about the result of the instruction.

## Examples

Schedule an **HVAC DR** control orchestration:

=== "Schedule orchestration"

	```sh
	s10k instructions controls orchestrate --node-id 101 \
	  --control-id 'HVAC DR' --exec-at 2026-07-16T17:48  \
	  --parameter mode:cool --parameter duration:PT2H
	```

=== "Output"

	```
	Control [HVAC DR] received orchestrate instruction 54188983.
	```

[OrchestrateControls]: https://solarnetwork.github.io/solarnode-handbook/users/instructions/topics/orchestrate-controls/
