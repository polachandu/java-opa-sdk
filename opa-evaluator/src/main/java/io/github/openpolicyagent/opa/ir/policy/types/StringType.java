package io.github.openpolicyagent.opa.ir.policy.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the parent's deserializer run again.
@JsonDeserialize
public class StringType implements Type {
  public static final String TypeMarker = "string";

  public StringType() {}

  @Override
  public String typeMarker() {
    return TypeMarker;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StringType that = (StringType) o;
    return Objects.equals(that.typeMarker(), TypeMarker);
  }

  @Override
  public int hashCode() {
    return Objects.hash(TypeMarker);
  }

  @Override
  public String toString() {
    return "StringType{}";
  }
}
