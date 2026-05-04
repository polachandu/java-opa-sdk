package io.github.openpolicyagent.opa.rego;

import java.util.HashMap;
import java.util.HashSet;
import io.github.openpolicyagent.opa.ast.types.RegoValue;

public class ResultSet {
  HashSet<HashMap<String, RegoValue>> results = new HashSet<>();

  public void addBindings(HashMap<String, RegoValue> bindings) {
    results.add(bindings);
  }

  public HashSet<HashMap<String, RegoValue>> getResults() {
    return results;
  }
}
