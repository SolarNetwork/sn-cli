# Profiles

To help make it easier to provide the necessary SolarNetwork credentials to the tool, you can create
named credential  _profiles_ that you can specify with the `--profile/-P` options, for example:

```sh
s10k --profile=home ...
```

You can also configure a **default profile** that will be used if no `--profile` option is provided.

## Credential profiles location

The profiles are stored in a `$HOME/.s10k/credentials` file, where `$HOME` is your system user's
"home" directory. For example on macOS for a user `alice` this might look like
`/Users/alice/.s10k/credentials` or on Linux `/home/alice/.s10k/credentials`.

!!! warning

	As you will be configuring your SolarNetwork credentials in this file, you should
	take care to configure limited permissions to the file.

Here is how you could set up the `credentials` file the first time, ensuring limited
permissions to the file:

=== "macOS/Linux"

	```sh
	mkdir ~/.s10k
	chmod 700 ~/.s10k
	touch ~/.s10k/credentials
	chmod 600 ~/.s10k/credentials
	```

=== "Windows"

	TODO

## Credential profiles structure

The credentials profile file is a [TOML](https://toml.io) file, where you can include **default**
credentials followed by named profiles, each that start with a `[NAME]` line, where `NAME` is the
name of the profile. Following that are `key = "value"` configuration lines. For example, here is a
`credentials` file with default credentials followed by two profiles named **home** and **work**:

```toml
sn_token_id = "DEFAULT_TOKEN_ID"
sn_token_secret = "DEFAULT_TOKEN_SECRET"

[home]
sn_token_id = "HOME_TOKEN_ID"
sn_token_secret = "HOME_TOKEN_SECRET"

[work]
sn_token_id = "WORK_TOKEN_ID"
sn_token_secret = "WORK_TOKEN_SECRET"
```

### Profile properties

The following properties are supported in each profile (including the default profile):

| Profile Property | Description |
|:-----------------|:------------|
| `sn_token_id`     | The SolarNetwork token identifier |
| `sn_token_secret` | The SolarNetwork token secret |
