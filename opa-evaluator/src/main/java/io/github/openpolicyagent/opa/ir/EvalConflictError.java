package io.github.openpolicyagent.opa.ir;

import io.github.openpolicyagent.opa.OpaException;

public class EvalConflictError extends OpaException {

    public EvalConflictError(String message) {
    super("eval_conflict_error", message, null);
    }
}
