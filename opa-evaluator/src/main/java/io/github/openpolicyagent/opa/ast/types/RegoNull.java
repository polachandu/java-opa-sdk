package io.github.openpolicyagent.opa.ast.types;

import com.fasterxml.jackson.annotation.JsonValue;

public class RegoNull implements RegoValue {

  public static final RegoNull INSTANCE = new RegoNull();

  private RegoNull() {}

    @JsonValue
    private Object getProperty() {
        return null;
    }

    @Override
    public Object nativeValue() {
        return null;
    }

    public String getTypeName() { return "null";}

  public boolean equals(Object o) {
    return o instanceof RegoNull;
  }

  @Override
  public String toString() {
    return "null";
  }
}
