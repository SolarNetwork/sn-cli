---
title: delete
---
# Sec-Tokens Delete

Delete a security token.

## Usage

```
s10k sec-tokens delete -id=tokenId
```

## Options

<div markdown="1" class="options-explicit-col-widths">

| Option | Long Version | Description |
|:-------|:-------------|:------------|
| `-id=` | `--identifier=` | the token ID to delete |

</div>

## Output

A success message.

## Examples

=== "Delete token"

	```sh
	s10k sec-tokens delete --identifier ZCv5FUjlTlIQGjigLhSW
	```

=== "Output"

	```
	Security token deleted.
	```

