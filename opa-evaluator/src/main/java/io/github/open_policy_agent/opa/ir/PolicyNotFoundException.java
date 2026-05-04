package io.github.open_policy_agent.opa.ir;

import io.github.open_policy_agent.opa.OpaException;

public class PolicyNotFoundException extends OpaException {
  public PolicyNotFoundException(String entrypoint) {
    super("policy_not_found", "No plan found for entrypoint: " + entrypoint, null);
    withContext("entrypoint", entrypoint);
  }
}
