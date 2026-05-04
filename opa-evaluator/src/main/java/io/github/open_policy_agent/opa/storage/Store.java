package io.github.open_policy_agent.opa.storage;

import java.util.Map;
import io.github.open_policy_agent.opa.ast.types.RegoObject;
import io.github.open_policy_agent.opa.bundle.Bundle;
import io.github.open_policy_agent.opa.ir.policy.Policy;

public interface Store {

  void write(String id, Bundle bundle, RegoObject value);

  RegoObject currentData();

  Policy getIrPolicyForEntrypoint(String entrypoint);

  Map<String, Bundle> getBundles();

  String getDefaultEntrypoint();
}
