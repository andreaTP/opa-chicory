[![CI](https://github.com/andreaTP/opa-chicory/workflows/CI/badge.svg)](https://github.com/andreaTP/opa-chicory)
[![](https://jitpack.io/v/andreaTP/opa-chicory.svg)](https://jitpack.io/#andreaTP/opa-chicory)

# Open Policy Agent WebAssemby Java SDK (experimental)

This is SDK for using WebAssembly (wasm) compiled [Open Policy Agent](https://www.openpolicyagent.org/) policies
with [Chicory](https://github.com/dylibso/chicory), a pure Java Wasm interpreter.

Initial implementation was based
on [Open Policy Agent WebAssemby NPM Module](https://github.com/open-policy-agent/npm-opa-wasm)
and [Open Policy Agent Ebassembly dotnet core SDK](https://github.com/me-viper/OpaDotNet)

## Why

We want fast in-process OPA policies evaluations, and avoid network bottlenecks when using [opa-java](https://github.com/StyraInc/opa-java).

# Getting Started

## Install the module

With Maven:

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

and:

```
<dependency>
    <groupId>com.github.andreaTP.opa-chicory</groupId>
    <artifactId>opa-chicory</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

## Usage

There are only a couple of steps required to start evaluating the policy.

...

## Requirements:

To run the tests you need the `opa` cli available on the `PATH` and `tar`.

## Build:

```
OPA_TESTSUITE=disabled mvn spotless:apply install
```

## Builtins:

We should have a section like this:
https://github.com/me-viper/OpaDotNet/blob/739b69ed22936455d1aa60909d8106184ef45c7f/docs/articles/Builtins.md

## TODO:

- CI - in progress
- Docs - to do
- ?
