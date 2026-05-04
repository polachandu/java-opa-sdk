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
public class ArrayType implements Type {
  public static final String TypeMarker = "array";

  @JsonProperty("static")
  private List<Type> staticItems;

  @JsonProperty("dynamic")
  private Type dynamic;

  public ArrayType() {}

  public ArrayType(List<Type> staticItems, Type dynamic) {
    this.staticItems = staticItems;
    this.dynamic = dynamic;
  }

  @Override
  public String typeMarker() {
    return TypeMarker;
  }

  public List<Type> getStaticItems() {
    return staticItems;
  }

  public void setStaticItems(List<Type> staticItems) {
    this.staticItems = staticItems;
  }

  public Type getDynamic() {
    return dynamic;
  }

  public void setDynamic(Type dynamic) {
    this.dynamic = dynamic;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArrayType arrayType = (ArrayType) o;

    if (!Objects.equals(staticItems, arrayType.staticItems)) return false;
    return Objects.equals(dynamic, arrayType.dynamic);
  }

  @Override
  public int hashCode() {
    int result = staticItems != null ? staticItems.hashCode() : 0;
    result = 31 * result + (dynamic != null ? dynamic.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ArrayType{" + "staticItems=" + staticItems + ", dynamic=" + dynamic + '}';
  }
}
