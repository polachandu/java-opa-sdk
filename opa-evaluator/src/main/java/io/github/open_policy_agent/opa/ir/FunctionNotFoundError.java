package io.github.open_policy_agent.opa.ir;

import io.github.open_policy_agent.opa.OpaException;

public class FunctionNotFoundError extends OpaException {
  public FunctionNotFoundError(String name) {
    super("function_not_found", "function not found: " + name, null);
    withContext("name", name);
  }
}
