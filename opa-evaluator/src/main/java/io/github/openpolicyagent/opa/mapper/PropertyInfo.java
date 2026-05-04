package io.github.openpolicyagent.opa.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Cached metadata for a single property, used by {@link RegoMapper} to convert between POJOs and
 * RegoValues without repeated reflection.
 *
 * <p>A property can be backed by a getter/setter pair (JavaBean convention) or by a direct field
 * reference (for public fields or {@code @JsonProperty}-annotated fields without getters).
 */
final class PropertyInfo {
  private final String jsonName;
  private final Method getter;
  private final Method setter;
  private final Field field;
  private final Class<?> rawType;
  private final Type genericType;
  private final boolean includeNonNull;

  PropertyInfo(
      String jsonName,
      Method getter,
      Method setter,
      Field field,
      Class<?> rawType,
      Type genericType,
      boolean includeNonNull) {
    this.jsonName = jsonName;
    this.getter = getter;
    this.setter = setter;
    this.field = field;
    this.rawType = rawType;
    this.genericType = genericType;
    this.includeNonNull = includeNonNull;
  }

  String getJsonName() {
    return jsonName;
  }

  Method getGetter() {
    return getter;
  }

  Method getSetter() {
    return setter;
  }

  Field getField() {
    return field;
  }

  Class<?> getRawType() {
    return rawType;
  }

  Type getGenericType() {
    return genericType;
  }

  boolean isIncludeNonNull() {
    return includeNonNull;
  }

  /** Read this property's value from the given object, using getter if available, else field. */
  Object readValue(Object pojo) throws ReflectiveOperationException {
    if (getter != null) {
      return getter.invoke(pojo);
    }
    if (field != null) {
      return field.get(pojo);
    }
    return null;
  }

  /** Write this property's value to the given object, using setter if available, else field. */
  void writeValue(Object pojo, Object value) throws ReflectiveOperationException {
    if (setter != null) {
      setter.invoke(pojo, value);
    } else if (field != null && !java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
      field.set(pojo, value);
    }
  }

  /** Returns true if this property can be read (has a getter or accessible field). */
  boolean isReadable() {
    return getter != null || field != null;
  }

  /** Returns true if this property can be written (has a setter or non-final field). */
  boolean isWritable() {
    return setter != null
        || (field != null && !java.lang.reflect.Modifier.isFinal(field.getModifiers()));
  }
}
