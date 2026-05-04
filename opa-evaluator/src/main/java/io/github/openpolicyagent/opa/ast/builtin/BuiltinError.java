package io.github.openpolicyagent.opa.ast.builtin;

import io.github.openpolicyagent.opa.OpaException;

public class BuiltinError extends OpaException {
    public BuiltinError(String message) {
    super("eval_builtin_error", message, null);
    }
}
