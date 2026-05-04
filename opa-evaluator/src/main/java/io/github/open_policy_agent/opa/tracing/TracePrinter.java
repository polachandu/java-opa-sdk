package io.github.open_policy_agent.opa.tracing;

import io.github.open_policy_agent.opa.ir.StatementEvent;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class TracePrinter {
    public static void printTrace(QueryTracer tracer, OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        
        AtomicInteger level = new AtomicInteger();
        tracer.getEvents().forEach(e -> {
            String indent = "| ".repeat(Math.max(level.get(), 0));

            String op = e.getOp().getValue();
            if (op.equals(Operation.ENTER.getValue())) {
                writer.append(indent)
                        .append(e instanceof StatementEvent ? ((StatementEvent) e).getStmt().toString() : e.toString())
                        .append("\n");
                level.incrementAndGet();
            } else if (op.equals(Operation.EXIT.getValue())) {
                level.decrementAndGet();
            } else if ((op.equals(Operation.BREAK.getValue()))) {
                writer.append(indent).append("break\n");
            }
        });

        writer.flush();
    }

    public static String traceToString(QueryTracer tracer) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        printTrace(tracer, out);
        return out.toString();
    }
}
