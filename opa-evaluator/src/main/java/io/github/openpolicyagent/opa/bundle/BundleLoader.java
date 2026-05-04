package io.github.openpolicyagent.opa.bundle;

import io.github.openpolicyagent.opa.storage.Store;

public interface BundleLoader {
  Bundle load(Store store);
}
