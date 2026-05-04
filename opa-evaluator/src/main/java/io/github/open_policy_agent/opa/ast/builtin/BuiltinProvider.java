package io.github.open_policy_agent.opa.ast.builtin;

import java.util.Map;
import java.util.function.BiFunction;
import io.github.open_policy_agent.opa.ast.types.RegoValue;
import io.github.open_policy_agent.opa.rego.EvaluationContext;

/**
 * SPI for contributing builtins to the BuiltinRegistry via {@link java.util.ServiceLoader}.
 *
 * <p>Implementations are registered in
 * {@code META-INF/services/io.github.open_policy_agent.opa.ast.builtin.BuiltinProvider} and are
 * automatically discovered and loaded by {@link BuiltinRegistry} at runtime.
 */
public interface BuiltinProvider {
  Map<String, BiFunction<EvaluationContext, RegoValue[], RegoValue>> builtins();
}