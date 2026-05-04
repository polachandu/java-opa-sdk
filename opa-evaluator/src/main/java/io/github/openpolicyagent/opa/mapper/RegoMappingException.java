package io.github.openpolicyagent.opa.mapper;

import io.github.openpolicyagent.opa.OpaException;

/**
 * Thrown when a POJO cannot be converted to/from a RegoValue.
 *
 * <p>Common causes:
 *
 * <ul>
 *   <li>The target class has no accessible no-arg constructor or @JsonCreator (reverse mapping)
 *   <li>A getter throws an exception (forward mapping)
 *   <li>Type mismatch between a RegoValue and the target Java type (reverse mapping)
 * </ul>
 */
public class RegoMappingException extends OpaException {

  public RegoMappingException(String message) {
    super("mapping_error", message, null);
  }

  public RegoMappingException(String message, Throwable cause) {
    super("mapping_error", message, cause);
  }
}
