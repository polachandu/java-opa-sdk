package io.github.openpolicyagent.opa.ast.types;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegoArray implements RegoValue, RegoCollection {
  private final List<RegoValue> values;

  public RegoArray() {
    values = new ArrayList<>();
  }

  public RegoArray(int cap) {
    values = new ArrayList<>(cap);
  }

  public RegoArray(List<RegoValue> values) {
    this.values = values;
  }

  public boolean contains(RegoValue value) {
    // Use utility methods for composite reference lookups
    if (value instanceof RegoArray) {
      return CompositeReferenceUtil.containsArray(this.values, (RegoArray) value);
    } else if (value instanceof RegoSet) {
      return CompositeReferenceUtil.containsSet(this.values, (RegoSet) value);
    } else if (value instanceof RegoObject) {
      return CompositeReferenceUtil.containsObject(this.values, (RegoObject) value);
    } else {
      return this.values.contains(value);
    }
  }

  public static RegoArray fromJsonNode(ObjectMapper mapper, JsonNode arrayNode) throws Exception {
    RegoArray array = new RegoArray();
    for (JsonNode element : arrayNode) {
      if (element.isObject()) {
        array.addValue(mapper.treeToValue(element, RegoObject.class));
      } else if (element.isArray()) {
        array.addValue(fromJsonNode(mapper, element));
      } else if (element.isTextual()) {
        array.addValue(new RegoString(element.asText()));
      } else if (element.isNumber()) {
        if (element.isIntegralNumber()) {
          array.addValue(new RegoBigInt(BigInteger.valueOf(element.asLong())));
        } else {
          array.addValue(new RegoDecimal(element.doubleValue()));
        }
      } else if (element.isBoolean()) {
        array.addValue(RegoBoolean.of(element.asBoolean()));
      } else if (element.isNull()) {
        array.addValue(RegoNull.INSTANCE);
      } else {
        throw new UnsupportedOperationException(
            "Unsupported JSON element type: " + element.getNodeType());
      }
    }
    return array;
  }

  @JsonValue
  public List<RegoValue> getValue() {
    return values;
  }

  public List<RegoValue> getValues() {
    return values;
  }

  public void addValue(RegoValue value) {
    values.add(value);
  }

  public Stream<RegoValue> valueStream() {
    return values.stream();
  }

  @Override
  public int length() {
    return values.size();
  }

  @Override
  public Object nativeValue() {
    return values.stream().map(RegoValue::nativeValue).collect(Collectors.toList());
  }

  public String getTypeName() {
    return "array";
  }

  @Override
  public String toString() {
    return values.toString();
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof RegoArray)) return false;

    return Objects.equals(values, ((RegoArray) o).values);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(values);
  }
}
