package io.github.openpolicyagent.opa;

public class InvalidInputException extends OpaException {
  public InvalidInputException(String message) {
    super("invalid_input", message, null);
  }
}
