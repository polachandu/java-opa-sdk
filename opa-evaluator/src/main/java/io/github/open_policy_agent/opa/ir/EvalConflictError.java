package io.github.open_policy_agent.opa.ir;

import io.github.open_policy_agent.opa.OpaException;

public class EvalConflictError extends OpaException {

    public EvalConflictError(String message) {
    super("eval_conflict_error", message, null);
    }
}
