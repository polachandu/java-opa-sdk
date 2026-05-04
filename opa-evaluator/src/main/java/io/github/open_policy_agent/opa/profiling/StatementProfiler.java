package io.github.open_policy_agent.opa.profiling;

import java.time.Duration;
import java.util.Map;
import io.github.open_policy_agent.opa.ir.stmts.Stmt;

/**
 * Interface for profiling statement execution during policy evaluation.
 *
 * <p>Statement profilers track execution time and frequency of individual statements (operators,
 * function calls, etc.) during policy evaluation. This is separate from location-based profiling
 * (see {@link io.github.open_policy_agent.opa.tracing.Profiler}) which tracks execution by source code
 * location.
 *
 * <p>Statement profiling answers questions like "How much time is spent in function X?" while
 * location profiling answers "How much time is spent on line 42 of policy.rego?"
 *
 * <p>Implementations should use exclusive timing - the time spent in a statement minus the time
 * spent in child statements.
 */
public interface StatementProfiler {

  /**
   * Called when a statement begins execution.
   *
   * @param stmt the statement that is starting
   */
  void startStatement(Stmt stmt);

  /**
   * Called when a statement completes execution.
   *
   * @param stmt the statement that completed
   * @param durationNanos the wall-clock duration in nanoseconds that the statement took to execute
   */
  void stopStatement(Stmt stmt, long durationNanos);

  /**
   * Get a summary of all statement executions.
   *
   * @return map of statement name to execution summary
   */
  Map<String, StatementSummary> getStatementSummaries();

  /**
   * Summary of statement execution statistics.
   *
   * <p>Tracks both the number of times a statement executed and the total/exclusive time spent.
   */
  final class StatementSummary {
    private final String name;
    private int count = 0;
    private Duration duration = Duration.ZERO;

    public StatementSummary(String name) {
      this.name = name;
    }

    void add(Duration dur) {
      duration = duration.plus(dur);
      count++;
    }

    void subtract(Duration dur) {
      duration = duration.minus(dur);
    }

    public String getName() {
      return name;
    }

    public int getCount() {
      return count;
    }

    public Duration getDuration() {
      return duration;
    }
  }
}
