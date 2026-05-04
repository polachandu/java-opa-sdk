package io.github.open_policy_agent.opa.jackson;

import io.github.open_policy_agent.opa.ir.PolicyReader;
import io.github.open_policy_agent.opa.ir.policy.Policy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

/**
 * Jackson-based {@link PolicyReader} implementation.
 *
 * <p>Parses OPA IR policy documents (JSON format) using a pre-configured
 * {@link ObjectMapper} with the {@link IrModule} registered.
 *
 * <p>This class is discovered automatically via {@link java.util.ServiceLoader}
 * (or JPMS {@code provides} declarations) — consumers do not need to reference it directly.
 */
public class JacksonPolicyReader implements PolicyReader {

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new IrModule())
          .registerModule(new JavaTimeModule());

  @Override
  public Policy read(InputStream in) throws IOException {
    return MAPPER.readValue(in, Policy.class);
  }
}