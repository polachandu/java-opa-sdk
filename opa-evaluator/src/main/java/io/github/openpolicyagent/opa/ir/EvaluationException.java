package io.github.openpolicyagent.opa.ir;

import io.github.openpolicyagent.opa.OpaException;

public class EvaluationException extends OpaException {
  public EvaluationException(String message) {
    super("eval_error", message, null);
  }

  public EvaluationException(String message, Throwable cause) {
    super("eval_error", message, cause);
  }
}
