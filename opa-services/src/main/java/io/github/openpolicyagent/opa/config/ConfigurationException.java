package io.github.openpolicyagent.opa.config;

import io.github.openpolicyagent.opa.OpaException;

public class ConfigurationException extends OpaException {
  public ConfigurationException(String message) {
    super("opa_config_error", message, null);
  }

  public ConfigurationException(String message, Throwable cause) {
    super("opa_config_error", message, cause);
  }
}
