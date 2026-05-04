package io.github.openpolicyagent.opa.ir;

import io.github.openpolicyagent.opa.tracing.Event;
import io.github.openpolicyagent.opa.tracing.Operation;

public class BlockEvent extends Event {
    public BlockEvent(Operation op) {
        super(op, null);
    }

    @Override
    public String toString() {
        return "Block";
    }
}
