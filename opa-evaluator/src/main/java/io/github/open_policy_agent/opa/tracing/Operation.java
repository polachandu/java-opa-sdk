package io.github.open_policy_agent.opa.tracing;

public enum Operation {
  ENTER("EnterStmt"),
  EXIT("ExitStmt"),
  BREAK("BreakStmt");

  private final String value;

  Operation(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
