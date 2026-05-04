package io.github.openpolicyagent.opa.ir;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.openpolicyagent.opa.ir.vals.Val;

/** Operand represents a value that a statement operates on. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Operand {
  @JsonProperty("value")
  private Val val;

  public Operand(Val val) {
    this.val = val;
  }

  public Operand() {}

  public Val getVal() {
    return val;
  }

  public void setVal(Val val) {
    this.val = val;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Operand operand = (Operand) o;

    return Objects.equals(val, operand.val);
  }

  @Override
  public int hashCode() {
    return val != null ? val.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Operand{" + "val=" + val + '}';
  }
}
