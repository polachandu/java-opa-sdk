package io.github.open_policy_agent.opa.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cached introspection metadata for a JavaBean class. Built once per class, then reused for all
 * subsequent conversions.
 *
 * <p>Property discovery follows Jackson conventions:
 *
 * <ol>
 *   <li>JavaBean getters ({@code getX()}/{@code isX()}) — always discovered
 *   <li>Public fields — discovered if no getter exists for the same property
 *   <li>{@code @JsonProperty}-annotated fields — discovered regardless of visibility
 * </ol>
 *
 * <p>Jackson annotations ({@code @JsonProperty}, {@code @JsonIgnore}, {@code @JsonInclude})
 * override defaults when present on the getter or the backing field.
 */
final class ClassInfo {
  private final List<PropertyInfo> properties;
  private final Constructor<?> noArgConstructor;
  private final CreatorInfo creatorInfo;

  private ClassInfo(
      List<PropertyInfo> properties, Constructor<?> noArgConstructor, CreatorInfo creatorInfo) {
    this.properties = Collections.unmodifiableList(properties);
    this.noArgConstructor = noArgConstructor;
    this.creatorInfo = creatorInfo;
  }

  List<PropertyInfo> getProperties() {
    return properties;
  }

  Constructor<?> getNoArgConstructor() {
    return noArgConstructor;
  }

  CreatorInfo getCreatorInfo() {
    return creatorInfo;
  }

  /**
   * Introspect a class and build its ClassInfo. Scans for JavaBean getters first, then fields that
   * weren't already discovered via getters.
   */
  static ClassInfo buildFor(Class<?> clazz) {
    Constructor<?> ctor = findNoArgConstructor(clazz);
    CreatorInfo creatorInfo = findJsonCreator(clazz);

    List<PropertyInfo> props = new ArrayList<>();
    Set<String> discoveredNames = new HashSet<>();
    Set<String> claimedFieldNames = new HashSet<>(); // fields claimed by getters (for dedup)

    // Phase 1: Discover properties via JavaBean getters (getX/isX)
    for (Method method : clazz.getMethods()) {
      if (!isGetter(method)) {
        continue;
      }

      String propertyName = derivePropertyName(method);
      Field backingField = findBackingField(clazz, propertyName);

      // For boolean is* getters, also look for the is-prefixed backing field.
      // e.g., isTrusted() derives "trusted" but the field is named "isTrusted".
      if (backingField == null && method.getName().startsWith("is")) {
        backingField = findBackingField(clazz, method.getName());
      }

      // Track claimed fields so Phase 2 doesn't re-discover them under a different name
      if (backingField != null) {
        claimedFieldNames.add(backingField.getName());
      }

      // Check @JsonIgnore on getter or field
      if (hasAnnotation(method, backingField, JsonIgnore.class)) {
        continue;
      }

      // Resolve JSON name: @JsonProperty overrides derived name
      String jsonName = resolveJsonName(method, backingField, propertyName);

      // Check @JsonInclude(NON_NULL) on getter or field
      boolean includeNonNull = hasIncludeNonNull(method, backingField);

      Class<?> rawType = method.getReturnType();
      Type genericType = method.getGenericReturnType();

      Method setter = findSetter(clazz, propertyName, rawType);
      setAccessibleQuietly(setter);
      setAccessibleQuietly(method);
      if (backingField != null && !setAccessibleQuietly(backingField)) {
        backingField = null; // can't access field (module system) — still use getter
      }

      props.add(
          new PropertyInfo(
              jsonName, method, setter, backingField, rawType, genericType, includeNonNull));
      discoveredNames.add(jsonName);
    }

    // Phase 2: Discover properties via fields not already found through getters.
    // Honors @JsonAutoDetect(fieldVisibility) when present; otherwise defaults to
    // public fields and @JsonProperty-annotated fields.
    JsonAutoDetect.Visibility fieldVisibility = resolveFieldVisibility(clazz);
    for (Field field : getAllFields(clazz)) {
      if (field.isAnnotationPresent(JsonIgnore.class)) {
        continue;
      }
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      // Skip fields already claimed by a getter (avoids isTrusted field + isTrusted() getter dupe)
      if (claimedFieldNames.contains(field.getName())) {
        continue;
      }

      boolean hasJsonProperty = field.isAnnotationPresent(JsonProperty.class);
      if (!hasJsonProperty && !isFieldVisible(field, fieldVisibility)) {
        continue;
      }

      JsonProperty jp = field.getAnnotation(JsonProperty.class);
      String jsonName = (jp != null && !jp.value().isEmpty()) ? jp.value() : field.getName();

      if (discoveredNames.contains(jsonName)) {
        continue;
      }

      boolean includeNonNull = false;
      JsonInclude ji = field.getAnnotation(JsonInclude.class);
      if (ji != null && ji.value() == JsonInclude.Include.NON_NULL) {
        includeNonNull = true;
      }

      if (!setAccessibleQuietly(field)) {
        continue;
      }

      Method setter = findSetter(clazz, field.getName(), field.getType());
      setAccessibleQuietly(setter);

      props.add(
          new PropertyInfo(
              jsonName, null, setter, field, field.getType(), field.getGenericType(),
              includeNonNull));
      discoveredNames.add(jsonName);
    }

    return new ClassInfo(props, ctor, creatorInfo);
  }

  /** Resolve field visibility from {@code @JsonAutoDetect}, defaulting to PUBLIC_ONLY. */
  private static JsonAutoDetect.Visibility resolveFieldVisibility(Class<?> clazz) {
    JsonAutoDetect ann = clazz.getAnnotation(JsonAutoDetect.class);
    if (ann != null && ann.fieldVisibility() != JsonAutoDetect.Visibility.DEFAULT) {
      return ann.fieldVisibility();
    }
    return JsonAutoDetect.Visibility.PUBLIC_ONLY;
  }

  /** Check if a field is visible under the given visibility level. */
  private static boolean isFieldVisible(Field field, JsonAutoDetect.Visibility visibility) {
    switch (visibility) {
      case ANY:
        return true;
      case NON_PRIVATE:
        return !Modifier.isPrivate(field.getModifiers());
      case PROTECTED_AND_PUBLIC:
        return Modifier.isPublic(field.getModifiers())
            || Modifier.isProtected(field.getModifiers());
      case NONE:
        return false;
      case PUBLIC_ONLY:
      default:
        return Modifier.isPublic(field.getModifiers());
    }
  }

  /** Collect all declared fields from the class and its superclasses. */
  private static List<Field> getAllFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    Class<?> current = clazz;
    while (current != null && current != Object.class) {
      for (Field f : current.getDeclaredFields()) {
        fields.add(f);
      }
      current = current.getSuperclass();
    }
    return fields;
  }

  private static boolean isGetter(Method method) {
    if (method.getParameterCount() != 0) {
      return false;
    }
    if (method.getReturnType() == void.class) {
      return false;
    }
    int mods = method.getModifiers();
    if (!Modifier.isPublic(mods) || Modifier.isStatic(mods)) {
      return false;
    }
    // Skip Object methods
    if (method.getDeclaringClass() == Object.class) {
      return false;
    }

    String name = method.getName();
    if (name.startsWith("get") && name.length() > 3) {
      return true;
    }
    if (name.startsWith("is")
        && name.length() > 2
        && (method.getReturnType() == boolean.class
            || method.getReturnType() == Boolean.class)) {
      return true;
    }
    return false;
  }

  /**
   * Derive property name from a getter method, matching Jackson's default (legacy) mangling.
   * Lowercases the entire leading uppercase run, stopping when a lowercase char is found.
   *
   * <p>Examples: {@code getName() → "name"}, {@code getURL() → "url"}, {@code isCKIdentityValid()
   * → "ckidentityValid"}, {@code isTrusted() → "trusted"}
   */
  private static String derivePropertyName(Method getter) {
    String name = getter.getName();
    int offset = name.startsWith("get") ? 3 : 2; // "get" or "is"
    int len = name.length();

    if (offset >= len) {
      return "";
    }

    // Single char after prefix — just lowercase it
    if (len - offset == 1) {
      return name.substring(offset).toLowerCase();
    }

    // Jackson's legacyManglePropertyName: lowercase the entire leading uppercase run
    char first = name.charAt(offset);
    char firstLower = Character.toLowerCase(first);
    if (first == firstLower) {
      // Already lowercase — return as-is
      return name.substring(offset);
    }

    StringBuilder sb = new StringBuilder(len - offset);
    sb.append(firstLower);
    for (int i = offset + 1; i < len; i++) {
      char c = name.charAt(i);
      char lower = Character.toLowerCase(c);
      if (c == lower) {
        // Hit a lowercase char — append the rest of the string unchanged and stop
        sb.append(name, i, len);
        break;
      }
      sb.append(lower);
    }
    return sb.toString();
  }

  private static Field findBackingField(Class<?> clazz, String propertyName) {
    Class<?> current = clazz;
    while (current != null && current != Object.class) {
      try {
        return current.getDeclaredField(propertyName);
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    return null;
  }

  private static <A extends Annotation> boolean hasAnnotation(
      Method getter, Field field, Class<A> annotationType) {
    if (getter.isAnnotationPresent(annotationType)) {
      return true;
    }
    return field != null && field.isAnnotationPresent(annotationType);
  }

  private static String resolveJsonName(Method getter, Field field, String defaultName) {
    // Method annotation takes precedence
    JsonProperty methodAnnotation = getter.getAnnotation(JsonProperty.class);
    if (methodAnnotation != null && !methodAnnotation.value().isEmpty()) {
      return methodAnnotation.value();
    }
    if (field != null) {
      JsonProperty fieldAnnotation = field.getAnnotation(JsonProperty.class);
      if (fieldAnnotation != null && !fieldAnnotation.value().isEmpty()) {
        return fieldAnnotation.value();
      }
    }
    return defaultName;
  }

  private static boolean hasIncludeNonNull(Method getter, Field field) {
    JsonInclude methodAnnotation = getter.getAnnotation(JsonInclude.class);
    if (methodAnnotation != null
        && methodAnnotation.value() == JsonInclude.Include.NON_NULL) {
      return true;
    }
    if (field != null) {
      JsonInclude fieldAnnotation = field.getAnnotation(JsonInclude.class);
      return fieldAnnotation != null
          && fieldAnnotation.value() == JsonInclude.Include.NON_NULL;
    }
    return false;
  }

  private static Method findSetter(Class<?> clazz, String propertyName, Class<?> type) {
    String setterName = "set" + Character.toUpperCase(propertyName.charAt(0))
        + propertyName.substring(1);
    try {
      return clazz.getMethod(setterName, type);
    } catch (NoSuchMethodException e) {
      // Try with boxed/unboxed variant
      Class<?> alt = boxingAlternative(type);
      if (alt != null) {
        try {
          return clazz.getMethod(setterName, alt);
        } catch (NoSuchMethodException e2) {
          // no setter found
        }
      }
      return null;
    }
  }

  private static Class<?> boxingAlternative(Class<?> type) {
    if (type == boolean.class) return Boolean.class;
    if (type == Boolean.class) return boolean.class;
    if (type == int.class) return Integer.class;
    if (type == Integer.class) return int.class;
    if (type == long.class) return Long.class;
    if (type == Long.class) return long.class;
    if (type == double.class) return Double.class;
    if (type == Double.class) return double.class;
    if (type == float.class) return Float.class;
    if (type == Float.class) return float.class;
    return null;
  }

  /**
   * Scan for a {@code @JsonCreator} annotated constructor or static factory method. Each parameter
   * must have {@code @JsonProperty} with an explicit name. Returns null if none found.
   */
  private static CreatorInfo findJsonCreator(Class<?> clazz) {
    // Check constructors first
    for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
      if (!ctor.isAnnotationPresent(JsonCreator.class)) {
        continue;
      }
      JsonCreator annotation = ctor.getAnnotation(JsonCreator.class);
      if (annotation.mode() == JsonCreator.Mode.DELEGATING) {
        continue;
      }
      List<CreatorInfo.CreatorParam> params = resolveCreatorParams(ctor.getParameters());
      if (params != null && setAccessibleQuietly(ctor)) {
        return CreatorInfo.forConstructor(ctor, params);
      }
    }

    // Check static factory methods
    for (Method method : clazz.getDeclaredMethods()) {
      if (!method.isAnnotationPresent(JsonCreator.class)) {
        continue;
      }
      if (!Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      JsonCreator annotation = method.getAnnotation(JsonCreator.class);
      if (annotation.mode() == JsonCreator.Mode.DELEGATING) {
        continue;
      }
      List<CreatorInfo.CreatorParam> params = resolveCreatorParams(method.getParameters());
      if (params != null && setAccessibleQuietly(method)) {
        return CreatorInfo.forFactory(method, params);
      }
    }

    return null;
  }

  /**
   * Resolve creator parameters. Returns null if any parameter lacks a {@code @JsonProperty} with a
   * non-empty value.
   */
  private static List<CreatorInfo.CreatorParam> resolveCreatorParams(Parameter[] parameters) {
    List<CreatorInfo.CreatorParam> params = new ArrayList<>();
    for (Parameter param : parameters) {
      CreatorInfo.CreatorParam cp = CreatorInfo.CreatorParam.fromParameter(param);
      if (cp == null) {
        return null;
      }
      params.add(cp);
    }
    return params;
  }

  private static Constructor<?> findNoArgConstructor(Class<?> clazz) {
    try {
      Constructor<?> ctor = clazz.getDeclaredConstructor();
      ctor.setAccessible(true);
      return ctor;
    } catch (NoSuchMethodException | RuntimeException e) {
      return null;
    }
  }

  /**
   * Attempt to make an {@link java.lang.reflect.AccessibleObject} accessible. Returns true on
   * success, false if the module system blocks it. Accepts null safely.
   */
  private static boolean setAccessibleQuietly(java.lang.reflect.AccessibleObject obj) {
    if (obj == null) {
      return false;
    }
    try {
      obj.setAccessible(true);
      return true;
    } catch (RuntimeException e) {
      return false;
    }
  }
}
