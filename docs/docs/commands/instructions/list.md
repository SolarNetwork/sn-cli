---
title: list
---
# Instructions List

Show instructions matching a search filter.

## Usage

```
s10k instructions list [-max=<maxDate>] [-min=<minDate>] [-tz=<zone>]
                       [-id=instructionId[,instructionId...]]...
                       [-node=nodeId[,nodeId...]]...
					   [-state=state[,state...]]...
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

You can show one exact instruction using its ID like this:

=== "Show instruction for ID"

	```sh
	s10k instructions list --node-id 101 --instruction-id 123
	```

=== "Output"

	```
	Property Value
	-----------------------------------------------------------
	id       123
	topic    SetControlParameter
	state    Completed
	date     2025-08-30T17:11:01.741346+12:00[Pacific/Auckland]
	params   Parameter     Value
	         -------------------
	         switch/1      1
	```

You can show all instructions

=== "Show completed instructions for node"

	```sh
	# using multiple options
	s10k instructions list --node-id 101 --state Completed
	```

=== "Output"

	```
	Property Value
	-----------------------------------------------------------
	id       123
	topic    SetControlParameter
	state    Completed
	date     2025-08-27T18:08:16.858044+12:00[Pacific/Auckland]
	params   Parameter     Value
	         -------------------
	         switch/1      1

	Property Value
	-----------------------------------------------------------
	id       124
	topic    SetControlParameter
	state    Completed
	date     2025-08-27T18:10:14.521734+12:00[Pacific/Auckland]
	params   Parameter     Value
	         -------------------
	         switch/1      0
	```


[instruction-states]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#node-instruction-state-type
