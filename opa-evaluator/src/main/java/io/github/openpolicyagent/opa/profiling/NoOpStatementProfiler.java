package io.github.openpolicyagent.opa.profiling;

import java.util.Collections;
import java.util.Map;
import io.github.openpolicyagent.opa.ir.stmts.Stmt;

/**
 * No-op implementation of StatementProfiler that does nothing.
 *
 * <p>This is the default profiler used when statement profiling is not needed. It has zero overhead
 * and is safe to use in production.
 */
public class NoOpStatementProfiler implements StatementProfiler {

  @Override
  public void startStatement(Stmt stmt) {
    // No-op
  }

  @Override
  public void stopStatement(Stmt stmt, long durationNanos) {
    // No-op
  }

  @Override
  public Map<String, StatementSummary> getStatementSummaries() {
    return Collections.emptyMap();
  }
}
