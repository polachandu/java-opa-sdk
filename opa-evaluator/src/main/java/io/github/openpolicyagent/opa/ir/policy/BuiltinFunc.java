package io.github.openpolicyagent.opa.ir.policy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import io.github.openpolicyagent.opa.ir.policy.types.FunctionType;

// BuiltinFunc represents a built-in function that may be required by the
// policy.
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class BuiltinFunc {
  @JsonProperty("name")
  private String name;

  @JsonProperty("decl")
  private FunctionType decl;

  public BuiltinFunc() {}

  public BuiltinFunc(String name, FunctionType decl) {
    this.name = name;
    this.decl = decl;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public FunctionType getDecl() {
    return decl;
  }

  public void setDecl(FunctionType decl) {
    this.decl = decl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BuiltinFunc that = (BuiltinFunc) o;

    if (!Objects.equals(name, that.name)) return false;
    return Objects.equals(decl, that.decl);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (decl != null ? decl.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "BuiltinFunc{" + "name='" + name + '\'' + ", decl=" + decl + '}';
  }
}
