package io.github.open_policy_agent.opa.rego;

import java.util.HashMap;
import java.util.HashSet;
import io.github.open_policy_agent.opa.ast.types.RegoValue;

public class ResultSet {
  HashSet<HashMap<String, RegoValue>> results = new HashSet<>();

  public void addBindings(HashMap<String, RegoValue> bindings) {
    results.add(bindings);
  }

  public HashSet<HashMap<String, RegoValue>> getResults() {
    return results;
  }
}
