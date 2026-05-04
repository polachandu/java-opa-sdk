# opa-jackson

Jackson-based OPA IR deserialization for the java-opa-sdk.

## Overview

This module provides the `JacksonPolicyReader`, a `PolicyReader` SPI implementation that deserializes OPA Intermediate Representation (IR) plans using Jackson. It is automatically discovered at runtime via `java.util.ServiceLoader` when loading bundles with `TarballBundleLoader`.

## How It Works

When the evaluator loads a bundle (e.g., `bundle.tar.gz`), it uses `ServiceLoader` to find a `PolicyReader` implementation. This module provides that implementation, which:

1. Reads the `plan.json` from the bundle
2. Deserializes the IR using custom Jackson deserializers registered via `IrModule`
3. Returns the parsed policy plan to the evaluator engine

## Dependencies

- `jackson-databind` - Core JSON processing
- `jackson-dataformat-yaml` - YAML support for configuration files
- `jackson-datatype-jsr310` - Java 8 date/time type support

## Typical Usage

No direct usage is required. Simply include this module on the classpath and it will be discovered automatically:

```java
// TarballBundleLoader uses ServiceLoader to find JacksonPolicyReader
Engine engine = new Engine.Builder()
    .withBundleLoader(new TarballBundleLoader("authz", Path.of("examples/policy.tar.gz")))
    .withEntrypoint("example/allow")
    .build();
```

## Key Classes

| Class | Description |
|-------|-------------|
| `JacksonPolicyReader` | `PolicyReader` SPI implementation using Jackson |
| `IrModule` | Jackson module registering custom IR type deserializers |