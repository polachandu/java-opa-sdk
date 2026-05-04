package io.github.open_policy_agent.opa;

public class InvalidInputException extends OpaException {
  public InvalidInputException(String message) {
    super("invalid_input", message, null);
  }
}
