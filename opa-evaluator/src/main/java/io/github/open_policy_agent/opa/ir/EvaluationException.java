package io.github.open_policy_agent.opa.ir;

import io.github.open_policy_agent.opa.OpaException;

public class EvaluationException extends OpaException {
  public EvaluationException(String message) {
    super("eval_error", message, null);
  }

  public EvaluationException(String message, Throwable cause) {
    super("eval_error", message, cause);
  }
}
