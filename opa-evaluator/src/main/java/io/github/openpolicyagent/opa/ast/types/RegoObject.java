package io.github.openpolicyagent.opa.ast.types;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.*;
import java.util.stream.Stream;

public class RegoObject implements RegoValue {
  @JsonIgnore private final Map<RegoValue, RegoValue> value;

  public RegoObject() {
    this.value = new LinkedHashMap<>();
  }

  public RegoObject(Map<RegoValue, RegoValue> properties) {
    this.value = new LinkedHashMap<>(properties);
  }

  public RegoValue getProperty(String property) {
    return value.get(new RegoString(property));
  }

  public RegoValue getProperty(RegoValue property) {
    return value.get(property);
  }

  public boolean hasProperty(RegoValue property) {
    return value.containsKey(property);
  }

  /**
   * For JSON serialization - Jackson uses this to convert to JSON. We convert RegoValue keys to
   * strings for JSON compatibility, and sort them to match OPA's output format.
   */
  @JsonValue
  public Map<String, RegoValue> getPropertiesAsStringMap() {
    // For JSON serialization, convert keys to strings and sort them
    // OPA (written in Go) outputs JSON with sorted keys, so we match that behavior
    Map<String, RegoValue> stringMap = new TreeMap<>();
    for (Map.Entry<RegoValue, RegoValue> entry : value.entrySet()) {
      String key = keyToString(entry.getKey());
      stringMap.put(key, entry.getValue());
    }
    return stringMap;
  }

  /** Get the internal map with RegoValue keys. This is used for runtime operations. */
  @JsonIgnore
  public Map<RegoValue, RegoValue> getProperties() {
    return value;
  }

  public RegoValue setProp(RegoValue property, RegoValue value) {
    return this.value.put(property, value);
  }

  /**
   * For JSON deserialization - Jackson calls this for each property. We convert string keys to
   * RegoString for internal storage.
   */
  @JsonAnySetter
  public void setProperty(String name, Object value) {
    // Convert the raw Object to appropriate RegoValue
    RegoValue regoValue = convertToRegoValue(value);
    // Try to parse the key as a number, otherwise use as string
    RegoValue key = parseKeyFromString(name);
    this.value.put(key, regoValue);
  }

  private RegoValue parseKeyFromString(String key) {
    // JSON object keys are always strings per the JSON spec.
    // OPA coerces integer references to string keys during data traversal (handled in the
    // evaluator's DotStmt), not at deserialization time.
    return new RegoString(key);
  }

  private String keyToString(RegoValue key) {
    if (key instanceof RegoString) {
      return ((RegoString) key).getValue();
    } else if (key instanceof RegoNumber) {
      return ((RegoNumber) key).getBigIntValue().toString();
    } else {
      return key.toString();
    }
  }

  public Stream<Map.Entry<RegoValue, RegoValue>> stream() {
    return value.entrySet().stream();
  }

  @Override
  public int length() {
    return value.size();
  }

  @Override
  public Object nativeValue() {
    Map<Object, Object> nativeMap = new LinkedHashMap<>();
    for (Map.Entry<RegoValue, RegoValue> entry : value.entrySet()) {
      nativeMap.put(entry.getKey().nativeValue(), entry.getValue().nativeValue());
    }
    return nativeMap;
  }

  private RegoValue convertToRegoValue(Object value) {
    if (value == null) {
      return RegoNull.INSTANCE;
    } else if (value instanceof RegoValue) {
      return (RegoValue) value;
    } else if (value instanceof Boolean) {
      return RegoBoolean.of((Boolean) value);
    } else if (value instanceof String) {
      return new RegoString((String) value);
    } else if (value instanceof Integer) {
      return RegoInt32.of((Integer) value);
    } else if (value instanceof Long) {
      return new RegoBigInt((Long) value);
    } else if (value instanceof java.math.BigDecimal) {
      // BigDecimal from Jackson when USE_BIG_DECIMAL_FOR_FLOATS is enabled
      return new RegoDecimal(((java.math.BigDecimal) value).doubleValue());
    } else if (value instanceof Double || value instanceof Float) {
      // Handle Double and Float
      return new RegoDecimal(((Number) value).doubleValue());
    } else if (value instanceof Map) {
      RegoObject obj = new RegoObject();
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
        if (entry.getKey() instanceof String) {
          obj.setProperty((String) entry.getKey(), convertToRegoValue(entry.getValue()));
        }
      }
      return obj;
    } else if (value instanceof java.util.List) {
      RegoArray array = new RegoArray();
      for (Object item : (java.util.List<?>) value) {
        array.addValue(convertToRegoValue(item));
      }
      return array;
    } else {
      // For numbers and other types, convert to string for now
      return new RegoString(value.toString());
    }
  }

  /**
   * Asymmetric recursive union of two objects. Conflicts are resolved by choosing the value from
   * the right-hand object (other). When both values are objects, they are recursively merged.
   *
   * @param other the right-hand object (wins on conflicts)
   * @return a new merged object
   */
  public RegoObject merge(RegoObject other) {
    RegoObject result = new RegoObject();

    // Start with all properties from this (left object)
    result.value.putAll(this.value);

    // Merge in properties from other (right object)
    for (Map.Entry<RegoValue, RegoValue> entry : other.value.entrySet()) {
      RegoValue key = entry.getKey();
      RegoValue rightValue = entry.getValue();
      RegoValue leftValue = result.value.get(key);

      if (leftValue == null) {
        // Key only exists in right object - add it
        result.value.put(key, rightValue);
      } else if (leftValue instanceof RegoObject && rightValue instanceof RegoObject) {
        // Both values are objects - recursively merge them
        RegoObject mergedValue = ((RegoObject) leftValue).merge((RegoObject) rightValue);
        result.value.put(key, mergedValue);
      } else {
        // Conflict: right object wins
        result.value.put(key, rightValue);
      }
    }

    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RegoObject that = (RegoObject) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    boolean first = true;
    for (Map.Entry<RegoValue, RegoValue> entry : value.entrySet()) {
      if (!first) {
        sb.append(",");
      }
      first = false;

      // Format the key - all keys need quotes in JSON
      RegoValue key = entry.getKey();
      if (key instanceof RegoString) {
        sb.append("\"").append(escapeJsonString(((RegoString) key).getValue())).append("\"");
      } else if (key instanceof RegoNumber) {
        sb.append("\"").append(key).append("\"");
      } else {
        // For complex types (objects, arrays, etc.), output as escaped JSON string
        sb.append("\"").append(escapeJsonString(key.toString())).append("\"");
      }

      sb.append(":");
      sb.append(entry.getValue().toString());
    }
    sb.append("}");
    return sb.toString();
  }

  private String escapeJsonString(String s) {
    StringBuilder sb = new StringBuilder();
    for (char c : s.toCharArray()) {
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < 0x20 || c == 0x7F) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
          break;
      }
    }
    return sb.toString();
  }

  @Override
  public String getTypeName() {
    return "object";
  }
}
