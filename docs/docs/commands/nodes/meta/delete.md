---
title: delete
---
# Nodes Meta Delete

Delete all metadata associated with a node.

# Usage

```
s10k nodes meta delete -node=<nodeId>
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-node=` | `--node-id=` | the node ID to delete metadata from |

</div>

## Output

A success message.

## Examples

```sh title="Delete metadata"
s10k --profile demo nodes meta delete --node-id 101
```
