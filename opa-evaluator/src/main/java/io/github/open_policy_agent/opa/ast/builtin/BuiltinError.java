package io.github.open_policy_agent.opa.ast.builtin;

import io.github.open_policy_agent.opa.OpaException;

public class BuiltinError extends OpaException {
    public BuiltinError(String message) {
    super("eval_builtin_error", message, null);
    }
}
