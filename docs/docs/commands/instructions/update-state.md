---
title: update-state
---
# Instructions Update-State

Change the state of instructions matching a search filter. Pass the desired [instruction
state][instruction-states] as the first (and only) parameter.

# Usage

```
s10k instructions update-state [-max=<maxDate>] [-min=<minDate>] [-tz=<zone>]
                       [-id=instructionId[,instructionId...]]...
                       [-node=nodeId[,nodeId...]]...
					   [-state=state[,state...]]...
					   <desiredState>
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-id=` | `--instruction-id=` | the instruction ID(s) to show |
| `-node=` | `--node-id=` | the node ID to show instructions for |
| `-state=` | `--state=` | an [instruction state][instruction-states] to limit results to |
| `-min=` | `--min-date=` | a minimum date to limit results to, like `2020-10-30` or `2020-10-30T12:45` |
| `-max=` | `--max-date=` | a maximum date (exclusive) to limit results to, in same form as `-min` |
| `-tz=` | `--time-zone=` | a time zone ID to treat the min/max dates as, instead of the local time zone, like `Pacific/Auckland` or `-05:00` or `UTC` |

</div>

## Output

A listing of all matching instruction records.

## Examples

Update a set of `Queued` instructions in a date range to `Declined`:

=== "Show instruction for ID"

	```sh
	s10k --profile demo instructions update-state --node-id 1011 --state Declined Received
	```

=== "Output"

	```
	Updated 6 instructions to Declined: 123,124,125,126,127,128
	```


[instruction-states]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#node-instruction-state-type
