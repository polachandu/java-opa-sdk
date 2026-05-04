package io.github.open_policy_agent.opa.ir;

import io.github.open_policy_agent.opa.ir.policy.Policy;
import java.io.IOException;
import java.io.InputStream;

/**
 * SPI for deserializing an OPA IR policy from a byte stream.
 *
 * <p>Register implementations via {@link java.util.ServiceLoader} or, when using JPMS,
 * via a {@code provides} declaration in {@code module-info.java}.
 *
 * <p>Example ServiceLoader wiring in a module:
 *
 * <pre>{@code
 * PolicyReader reader = ServiceLoader.load(PolicyReader.class)
 *     .findFirst()
 *     .orElseThrow(() -> new IllegalStateException(
 *         "No PolicyReader found on the classpath. " +
 *         "Add a module that provides PolicyReader (e.g. opa-jackson)."));
 * }</pre>
 */
public interface PolicyReader {
  Policy read(InputStream in) throws IOException;
}