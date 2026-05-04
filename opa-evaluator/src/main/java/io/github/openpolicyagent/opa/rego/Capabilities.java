package io.github.openpolicyagent.opa.rego;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import io.github.openpolicyagent.opa.ast.builtin.Descriptor;

/**
 * Represents OPA capabilities, including available builtin functions. Can be loaded from JSON files
 * matching the opa-cap.json format.
 */
public class Capabilities {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
  }

  @JsonProperty("builtins")
  public List<Descriptor> builtins = new ArrayList<>();

  public Capabilities() {}

  public Capabilities(List<Descriptor> builtins) {
    this.builtins = builtins;
  }

  /** Load capabilities from a JSON string */
  public static Capabilities fromJson(String json) throws IOException {
    return MAPPER.readValue(json, Capabilities.class);
  }

  /** Convert capabilities to JSON string */
  public String toJson() throws IOException {

    return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this);
  }
}
