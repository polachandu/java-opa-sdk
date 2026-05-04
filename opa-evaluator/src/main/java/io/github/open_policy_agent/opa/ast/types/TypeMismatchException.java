package io.github.open_policy_agent.opa.ast.types;

import java.util.Arrays;
import io.github.open_policy_agent.opa.OpaException;

public class TypeMismatchException extends OpaException {
  public TypeMismatchException(String expected, String actual) {
    super("type_mismatch", String.format("Expected type %s but got %s", expected, actual), null);
    withContext("expected", expected);
    withContext("actual", actual);
  }

  public TypeMismatchException(String[] expected, String actual) {
    super(
        "type_mismatch",
        String.format("Expected types %s but got %s", Arrays.toString(expected), actual),
        null);
    withContext("expected", expected);
    withContext("actual", actual);
  }
}
