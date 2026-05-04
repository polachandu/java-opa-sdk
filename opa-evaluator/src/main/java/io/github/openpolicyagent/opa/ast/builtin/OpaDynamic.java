package io.github.openpolicyagent.opa.ast.builtin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Describes dynamic type information for OPA builtin function arguments. Used within @OpaType to
 * specify the inner type of collections.
 *
 * <p>Supports either: - A simple type via the 'type' field (e.g., for arrays) - Key and value types
 * via 'key' and 'value' fields (e.g., for objects)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OpaDynamic {
  /**
   * Simple type for array elements. Example: array with dynamic type "any" means array of any type
   * Mutually exclusive with key/value.
   */
  String type() default "";

  /**
   * Key type string for object/map types (e.g., "string", "number"). Must be used together with
   * 'valueType'. Mutually exclusive with 'type'.
   */
  String keyType() default "";

  /**
   * Value type string for object/map types (e.g., "string", "number"). Must be used together with
   * 'keyType'. Mutually exclusive with 'type'.
   */
  String valueType() default "";
}
