package io.github.openpolicyagent.opa.storage;

import io.github.openpolicyagent.opa.OpaException;

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
