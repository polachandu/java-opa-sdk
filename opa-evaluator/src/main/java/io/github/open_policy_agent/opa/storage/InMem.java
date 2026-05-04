package io.github.open_policy_agent.opa.storage;

import io.github.open_policy_agent.opa.ast.types.RegoObject;
import io.github.open_policy_agent.opa.ir.policy.Policy;

/** In-memory implementation of the Store interface. */
public class InMem extends AbstractStore {

  private RegoObject data = new RegoObject();

  @Override
  protected void writeData(String bundleId, String root, RegoObject value) {
    // Write data to the appropriate location based on root
    if (root.isEmpty()) {
      // Empty root means write to the global data root
      data = data.merge(value);
    } else {
      // Non-empty root: write data under that path (e.g., "example" -> data.example)
      String[] pathParts = root.split("/");
      RegoObject current = data;

      // Navigate/create the path to the root
      for (int i = 0; i < pathParts.length - 1; i++) {
        io.github.open_policy_agent.opa.ast.types.RegoString key =
            new io.github.open_policy_agent.opa.ast.types.RegoString(pathParts[i]);
        io.github.open_policy_agent.opa.ast.types.RegoValue existing = current.getProperty(key);
        if (existing instanceof RegoObject) {
          current = (RegoObject) existing;
        } else {
          RegoObject newObj = new RegoObject();
          current.setProp(key, newObj);
          current = newObj;
        }
      }

      // Set the value at the final path segment
      io.github.open_policy_agent.opa.ast.types.RegoString finalKey =
          new io.github.open_policy_agent.opa.ast.types.RegoString(pathParts[pathParts.length - 1]);
      io.github.open_policy_agent.opa.ast.types.RegoValue existingValue = current.getProperty(finalKey);
      if (existingValue instanceof RegoObject) {
        // Merge with existing object
        current.setProp(finalKey, ((RegoObject) existingValue).merge(value));
      } else {
        // Set new value
        current.setProp(finalKey, value);
      }
    }
  }

  @Override
  public Policy getIrPolicyForEntrypoint(String entrypoint) {
    // Spec says that for a blank entrypoint you should return the first plan, but with multiple
    // bundles "first" is a little random, so there might need to be some clarification here
    return getBundles().values().stream()
        .filter(b -> b.irPolicy != null)
        .filter(
            b ->
                b.irPolicy.getPlans().getPlans().stream()
                    .anyMatch(p -> p.getName().equals(entrypoint)))
        .findFirst()
        .map(b -> b.irPolicy)
        .orElseGet(
            () ->
                getBundles().values().stream()
                    .filter(b -> b.irPolicy != null)
                    .map(b -> b.irPolicy)
                    .findFirst()
                    .orElse(null));
  }

  @Override
  public RegoObject currentData() {
    return data;
  }
}
