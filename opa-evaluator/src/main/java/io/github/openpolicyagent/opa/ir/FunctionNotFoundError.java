package io.github.openpolicyagent.opa.ir;

import io.github.openpolicyagent.opa.OpaException;

public class FunctionNotFoundError extends OpaException {
  public FunctionNotFoundError(String name) {
    super("function_not_found", "function not found: " + name, null);
    withContext("name", name);
  }
}
