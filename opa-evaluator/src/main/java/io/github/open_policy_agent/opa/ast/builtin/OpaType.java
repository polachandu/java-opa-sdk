package io.github.open_policy_agent.opa.ast.builtin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Describes a type in OPA builtin function signatures. Used for both arguments and return values
 * in @OpaBuiltin annotations.
 *
 * <p>Supports either: - A single type via the 'type' field - Multiple alternative types (union) via
 * the 'of' field - A dynamic type specification via the 'dynamic' field
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OpaType {
  /**
   * Single type string (e.g., "string", "number", "array", "object"). Mutually exclusive with 'of'.
   */
  String type() default "";

  /**
   * Array of alternative type values for union types. Example: count accepts array OR set, so you'd
   * use: of = { @OpaVal("array[any]"), @OpaVal("set[any]") } Mutually exclusive with 'type'.
   */
  OpaVal[] of() default {};

  /**
   * Dynamic type specification for variadic or dynamic arguments. Example: sprintf values parameter
   * uses: type = "array", dynamic = @OpaDynamic(type = "any") Example: object with key/value types:
   * type = "object", dynamic = @OpaDynamic(keyType = "string", valueType = "string")
   */
  OpaDynamic dynamic() default @OpaDynamic();

  /** Name of this argument (for arguments only, not for result). */
  String name() default "";

  /** Description of this type/argument. */
  String description() default "";
}
