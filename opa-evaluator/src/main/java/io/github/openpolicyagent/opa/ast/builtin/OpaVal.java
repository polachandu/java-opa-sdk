package io.github.openpolicyagent.opa.ast.builtin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a single type value in OPA type system. Used within @OpaType to describe individual
 * types.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OpaVal {
  /** The type string (e.g., "string", "number", "array[any]", "object", "set[string]") */
  String value();
}
