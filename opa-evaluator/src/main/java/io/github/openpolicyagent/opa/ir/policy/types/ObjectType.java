package io.github.openpolicyagent.opa.ir.policy.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Objects;
import io.github.openpolicyagent.opa.ir.ObjectTypeDynamicProperty;
import io.github.openpolicyagent.opa.ir.ObjectTypeStaticProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the parent's deserializer run again.
@JsonDeserialize
public class ObjectType implements Type {
  public static final String TypeMarker = "object";

  @JsonProperty("static")
  private List<ObjectTypeStaticProperty> statics;

  @JsonProperty("dynamic")
  private ObjectTypeDynamicProperty dynamics;

  public ObjectType() {}

  public ObjectType(List<ObjectTypeStaticProperty> statics, ObjectTypeDynamicProperty dynamics) {
    this.statics = statics;
    this.dynamics = dynamics;
  }

  @Override
  public String typeMarker() {
    return TypeMarker;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ObjectType that = (ObjectType) o;

    if (!Objects.equals(statics, that.statics)) return false;
    return Objects.equals(dynamics, that.dynamics);
  }

  @Override
  public int hashCode() {
    int result = statics != null ? statics.hashCode() : 0;
    result = 31 * result + (dynamics != null ? dynamics.hashCode() : 0);
    return result;
  }

  public List<ObjectTypeStaticProperty> getStatics() {
    return statics;
  }

  public void setStatics(List<ObjectTypeStaticProperty> statics) {
    this.statics = statics;
  }

  public ObjectTypeDynamicProperty getDynamics() {
    return dynamics;
  }

  public void setDynamics(ObjectTypeDynamicProperty dynamics) {
    this.dynamics = dynamics;
  }

  @Override
  public String toString() {
    return "ObjectType{" + "statics=" + statics + ", dynamics=" + dynamics + '}';
  }
}
