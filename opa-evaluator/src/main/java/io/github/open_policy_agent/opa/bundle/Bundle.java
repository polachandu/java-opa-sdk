package io.github.open_policy_agent.opa.bundle;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import io.github.open_policy_agent.opa.ir.policy.Policy;

public class Bundle {
  //  public RegoObject data;
  public final Policy irPolicy;
  public final JsonNode manifest;
  public final Map<String, String> rego;

  private Bundle(Builder builder) {
    this.rego = builder.rego;
    this.manifest = builder.manifest;
    this.irPolicy = builder.irPolicy;
  }

  public static class Builder {
    private final Map<String, String> rego = new HashMap<>();
    private JsonNode manifest;
    private Policy irPolicy;

    public Builder withRego(String path, String rego) {
      this.rego.put(path, rego);
      return this;
    }

    public Builder withManifest(JsonNode manifest) {
      this.manifest = manifest;
      return this;
    }

    public Builder withIrPolicy(Policy irPolicy) {
      this.irPolicy = irPolicy;
      return this;
    }

    public Builder withPolicy(Policy policy) {
      this.irPolicy = policy;
      return this;
    }

    public Bundle build() {
      return new Bundle(this);
    }
  }
}
