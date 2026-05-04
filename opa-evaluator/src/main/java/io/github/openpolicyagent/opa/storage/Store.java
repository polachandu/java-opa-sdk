package io.github.openpolicyagent.opa.storage;

import java.util.Map;
import io.github.openpolicyagent.opa.ast.types.RegoObject;
import io.github.openpolicyagent.opa.bundle.Bundle;
import io.github.openpolicyagent.opa.ir.policy.Policy;

public interface Store {

  void write(String id, Bundle bundle, RegoObject value);

  RegoObject currentData();

  Policy getIrPolicyForEntrypoint(String entrypoint);

  Map<String, Bundle> getBundles();

  String getDefaultEntrypoint();
}
