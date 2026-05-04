package io.github.openpolicyagent.opa.ir.policy.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the parent's deserializer run again.
@JsonDeserialize
public class BooleanType implements Type {
  public static final String TypeMarker = "boolean";

  public BooleanType() {}

  @Override
  public String typeMarker() {
    return TypeMarker;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BooleanType that = (BooleanType) o;
    return Objects.equals(that.typeMarker(), TypeMarker);
  }

  @Override
  public int hashCode() {
    return Objects.hash(TypeMarker);
  }

  @Override
  public String toString() {
    return "BooleanType{}";
  }
}
