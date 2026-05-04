package io.github.open_policy_agent.opa.ir;

import io.github.open_policy_agent.opa.tracing.Event;
import io.github.open_policy_agent.opa.tracing.Operation;

public class BlockEvent extends Event {
    public BlockEvent(Operation op) {
        super(op, null);
    }

    @Override
    public String toString() {
        return "Block";
    }
}
