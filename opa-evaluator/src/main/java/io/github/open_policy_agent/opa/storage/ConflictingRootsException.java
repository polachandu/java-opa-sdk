package io.github.open_policy_agent.opa.storage;

import io.github.open_policy_agent.opa.OpaException;

/**
 * Exception thrown when multiple bundles attempt to write to conflicting root paths.
 *
 * <p>Two roots conflict if:
 *
 * <ul>
 *   <li>They are exactly the same path
 *   <li>One is a prefix of the other (e.g., "example" conflicts with "example/foo")
 * </ul>
 */
public class ConflictingRootsException extends OpaException {
  public ConflictingRootsException(String message) {
    super("opa_conflicting_roots", message, null);
  }
}
