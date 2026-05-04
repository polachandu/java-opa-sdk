package io.github.openpolicyagent.opa.ir;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import io.github.openpolicyagent.opa.ir.policy.types.Type;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectTypeDynamicProperty {
  @JsonProperty("key")
  private Type key;

  @JsonProperty("value")
  private Type value;

  public ObjectTypeDynamicProperty() {}

  public ObjectTypeDynamicProperty(Type key, Type value) {
    this.key = key;
    this.value = value;
  }

  public Type getKey() {
    return key;
  }

  public void setKey(Type key) {
    this.key = key;
  }

  public Type getValue() {
    return value;
  }

  public void setValue(Type value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ObjectTypeDynamicProperty that = (ObjectTypeDynamicProperty) o;

    if (!Objects.equals(key, that.key)) return false;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ObjectTypeDynamicProperty{" + "key=" + key + ", value=" + value + '}';
  }
}
