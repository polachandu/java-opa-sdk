package io.github.open_policy_agent.opa.bundle;

import io.github.open_policy_agent.opa.storage.Store;

public interface BundleLoader {
  Bundle load(Store store);
}
