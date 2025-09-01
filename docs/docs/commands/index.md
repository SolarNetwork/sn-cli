# Commands

The `s10k` tool works as a set of commands and sub-commands, in a sort of command hierachy.

## Command options

Commands are passed options, which can come in _short_ or _long_ varieties. Short options
start with a single dash `-` and long options start with a double dash `--`. For example
the "be more verbose" [global opiton](../global-options.md) can be specified as the
short option `-v` or the long option `--verbose`.

### Toggle options

Some options just toggle a feature on or off. They do not require an associated value.

### Value options

Some options require an associated _value_. The value can be provided after the option and a space or
equal sign followed by the desired value. The value can be enclosed in quotes if it includes spaces
or other special characters. In the command documentation, options that require a value are shown
with an `=` sign, for example the [list datum](./datum/list.md) command supports the `-M=` or
`--max=` option for a maximum number of results. If you wanted the result to include a maximum of
`10` results, you could specify that in any of the following ways:

 * `-M 10`
 * `-M=10`
 * `--max 10`
 * `--max "10"`
 * `--max=10`
 * `--max="10"`

## Argument files

If you have a repeated set of arguments that you use frequently, you can stash those options in an
_argument file_ and then specify that as an `@`-prefixed path argument. The file content will be
treated as arguments, as if they had been passed directly on the command line. The file can be split
across lines and even include `#` prefixed comment lines.

For example, if you like to [list datum](./datum/list.md) for the same node and source ID
frequently, you could create an argument file like this:

``` title="fav-list-hourly.args"
# list hourly datum for my favorite datum stream in CSV format
--profile demo
 datum list
 --display-mode CSV
 --node-id 101
 --source-id con/1
 --aggregate Hour
```

Then you could list the datum like this:

```sh
s10k @fav-list-hourly.args --min-date 2025-08-01 --max-date 2025-08-02
```

Notice how you can still provide additional arguments after the `@` argument.

!!! tip

	The argument files can themselves include other argument files, by
	including another `@` argument, for example `@other-file.args`.

!!! warning

	If you need to quote an option value in an argument file, you must
	use a space between the option and the value, not an `=` character.
	For example `--myarg "value with spaces".

## Command parameters

Some commands accept _parameters_, which are arguments passed to the command after all
options. The syntax of the parameters are command-specific so refer to a command's
documentation for more information.

## File parameters

Some commands accept reading input from a file, and you can specify the path to the file after a
`@@` prefix. For example the [node meta save](./nodes/meta/save.md) command accepts the metadata to
save in this manner.

!!! tip

	When running in a command shell, redirection and pipes can achieve the same thing as `@@` file
	input. For example in `sh` compatible shells, `... @@my-file.json` could instead be written like
	`<my-file.json`
