package io.github.openpolicyagent.opa.logging;

public interface Logger {

  void debug(String fmtMessage, Object... args);

  void info(String fmtMessage, Object... args);

  void warn(String fmtMessage, Object... args);

  void error(String fmtMessage, Object... args);

  class StandardLogger implements Logger {

    public void debug(String fmtMessage, Object... args) {
      System.out.printf(("DEBUG: " + fmtMessage) + "%n", args);
    }

    public void info(String fmtMessage, Object... args) {
      System.out.printf(("INFO: " + fmtMessage) + "%n", args);
    }

    public void warn(String fmtMessage, Object... args) {
      System.out.printf(("WARN: " + fmtMessage) + "%n", args);
    }

    public void error(String fmtMessage, Object... args) {
      System.err.printf(("ERROR: " + fmtMessage) + "%n", args);
      System.out.printf(("ERROR: " + fmtMessage) + "%n", args);
    }
  }
}
