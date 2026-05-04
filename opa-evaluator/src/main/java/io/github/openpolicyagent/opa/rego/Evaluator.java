package io.github.openpolicyagent.opa.rego;

import io.github.openpolicyagent.opa.ast.types.RegoObject;
import io.github.openpolicyagent.opa.ast.types.RegoValue;
import io.github.openpolicyagent.opa.ir.PreparedPlan;

public interface Evaluator {
  // TODO: Need a better type for input
  RegoValue[] evaluate(EvaluationContext ctx, RegoValue input, RegoObject data);

  /**
   * Optimized evaluation using a pre-warmed plan.
   *
   * @param preparedPlan the pre-warmed plan containing cached values
   * @param ctx the evaluation context
   * @param input the input value
   * @param data the data object
   * @return array of result values
   */
  RegoValue[] evaluate(
      PreparedPlan preparedPlan, EvaluationContext ctx, RegoValue input, RegoObject data);
}
