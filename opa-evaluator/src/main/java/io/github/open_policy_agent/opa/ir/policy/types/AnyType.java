package io.github.open_policy_agent.opa.ir.policy.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the parent's deserializer run again.
@JsonDeserialize
public class AnyType implements Type {
  public static final String TypeMarker = "any";

  @JsonProperty("of")
  private List<Type> of;

  public AnyType() {}

  public AnyType(List<Type> of) {
    this.of = of;
  }

  @Override
  public String typeMarker() {
    return TypeMarker;
  }

  public List<Type> getOf() {
    return of;
  }

  public void setOf(List<Type> of) {
    this.of = of;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AnyType anyType = (AnyType) o;

    return Objects.equals(of, anyType.of);
  }

  @Override
  public int hashCode() {
    return of != null ? of.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "AnyType{" + "of=" + of + '}';
  }
}
