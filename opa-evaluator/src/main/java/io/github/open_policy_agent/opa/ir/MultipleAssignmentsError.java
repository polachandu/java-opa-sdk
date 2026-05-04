package io.github.open_policy_agent.opa.ir;

import io.github.open_policy_agent.opa.OpaException;

public class MultipleAssignmentsError extends OpaException {

    private final boolean categorized;

    public MultipleAssignmentsError() {
    super("eval_conflict_error", "complete rules must not produce multiple outputs", null);
        this.categorized = false;
    }

    public MultipleAssignmentsError(String message) {
    super("eval_conflict_error", message, null);
        this.categorized = true;
    }

    /** Returns true if this error has already been categorized (e.g., as a function conflict). */
    public boolean isCategorized() {
        return categorized;
    }
}
