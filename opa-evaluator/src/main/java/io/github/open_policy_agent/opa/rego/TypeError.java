package io.github.open_policy_agent.opa.rego;

import io.github.open_policy_agent.opa.OpaException;

public class TypeError extends OpaException {
    private final String rawCause;
    public TypeError(String message) {
        super("eval_type_error", message, null);
        this.rawCause = message;
    }

    public TypeError(String message, TypeError cause) {
        super("eval_type_error", message + ": " + cause.rawCause, cause);
        this.rawCause = cause.rawCause;
    }
}
