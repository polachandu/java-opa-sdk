package io.github.open_policy_agent.opa.ast.builtin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Descriptor for an OPA builtin function, matching the format from opa-cap.json */
public class Descriptor {
  @JsonProperty("name")
  public String name;

  @JsonProperty("description")
  public String description;

  @JsonProperty("categories")
  public List<String> categories;

  @JsonProperty("decl")
  public Declaration decl;

  @JsonProperty("infix")
  public String infix;

  @JsonProperty("nondeterministic")
  public Boolean nondeterministic;

  public Descriptor() {}

  public Descriptor(String name) {
    this.name = name;
  }

  public Descriptor(String name, Declaration decl) {
    this.name = name;
    this.decl = decl;
  }

  public static class Declaration {
    @JsonProperty("type")
    public String type = "function";

    @JsonProperty("args")
    public List<Argument> args;

    @JsonProperty("result")
    public Argument result;

    public Declaration() {}

    public Declaration(List<Argument> args, Argument result) {
      this.args = args;
      this.result = result;
    }
  }

  public static class Argument {
    @JsonProperty("type")
    public String type;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("of")
    public List<Argument> of;

    @JsonProperty("dynamic")
    public Dynamic dynamic;

    public Argument() {}

    public Argument(String type) {
      this.type = type;
    }

    public Argument(String type, String name, String description) {
      this.type = type;
      this.name = name;
      this.description = description;
    }

    public Argument(List<Argument> of) {
      this.type = "any";
      this.of = of;
    }
  }

  /** Represents dynamic type information for collections */
  public static class Dynamic {
    @JsonProperty("type")
    public String type;

    @JsonProperty("key")
    public Argument key;

    @JsonProperty("value")
    public Argument value;

    public Dynamic() {}

    public Dynamic(String type) {
      this.type = type;
    }

    public Dynamic(Argument key, Argument value) {
      this.key = key;
      this.value = value;
    }
  }
}
