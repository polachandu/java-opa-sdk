package io.github.openpolicyagent.opa.rego;

public enum TraceLevel {
  ALL("all"),
  NOTES("notes"),
  OFF("off");

  private final String value;

  TraceLevel(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
