---
title: Instructions
---
# Instructions Commands

The `instructions` group of commands deal with SolarNode instructions, which are actions you can ask
a node to perform, and the node confirms the outcome of the action, possibly providing result data.

Instructions in SolarNetwork have a **topic** that is the name of an action the node should perform.
Examples of [topics][instruction-topics] are `SetControlParameter` to set a control to some value,
or `SystemReboot` to reboot the node device.

Instructions then have an associated **state** that evolves over the lifetime of the instruction.
In general an instruction transitions between the following states:

| State | Description |
|:------|:------------|
| `Queuing` | The instruction is being queued for delivery to the node, but not acknowledged by the node yet. |
| `Queued` | The instruction has been queued for delivery to the node, but not acknowledged by the node yet. |
| `Received` | The instruction has been delivered to the node and the node has acknowledged receiving the instruction, but has not been executed yet. |
| `Executing` | The node is currently executing the instruction. |
| `Completed` | The node has finished executing the instruction. |

There is also a `Declined` state, when the node has rejected the instruction and will not execute it.

Instructions are processed asynchronously, and instructions proceed through these states over time
as instructions created in SolarNetwork and then transmitted to the node, executed on the node, and
finally reported as `Completed` or `Declined`.

For more details on how instructions work, see [here][instruction-add].

!!! info

	Instruction records are purged from SolarNetwork automatically over time, once they reach
	their final `Completed` or `Declined` state.

[instruction-add]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API#queue-instruction
[instruction-topics]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarUser-API-enumerated-types#node-instruction-topics
