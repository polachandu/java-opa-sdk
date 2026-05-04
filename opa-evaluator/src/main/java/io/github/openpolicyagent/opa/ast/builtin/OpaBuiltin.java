package io.github.openpolicyagent.opa.ast.builtin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark and document OPA builtin functions. This annotation is used to generate
 * capabilities JSON and documentation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OpaBuiltin {
  /** The OPA builtin name (e.g., "concat", "strings.reverse") */
  String name();

  /** Description of what the builtin does */
  String description() default "";

  /** Categories this builtin belongs to (e.g., "strings", "numbers", "aggregates") */
  String[] categories() default {};

  /** Argument specifications with names, types, and descriptions. */
  OpaType[] args() default {};

  /** Return type specification with type and optional description. */
  OpaType result();

  /** The infix operator if it has one */
  String infix() default "";

  /**
   * Whether this builtin is nondeterministic. Nondeterministic builtins may return different values
   * for the same inputs across evaluations (e.g., time.now_ns, http.send, rand.intn). These
   * builtins are eligible for nd_builtin_cache support.
   */
  boolean nondeterministic() default false;
}
