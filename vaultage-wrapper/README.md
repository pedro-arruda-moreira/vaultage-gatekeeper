# vaultage-wrapper

This project is designed to be used with [vaultage-gatekeeper](https://github.com/pedro-arruda-moreira/vaultage-gatekeeper), although it can be used isolated from it.

## Goal
This project will start listening on port 3001 (or 3002, 3003.... if 3001 is not available) and will generate a UUID based token. If this token is received via REST, this project (and consequently [vaultage-server](https://github.com/vaultage-pm/vaultage/tree/master/packages/vaultage)) will be shutdown gracefully.
