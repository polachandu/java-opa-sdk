package io.github.open_policy_agent.opa.plugins;

import io.github.open_policy_agent.opa.OpaException;

/**
 * Exception thrown when plugin initialization fails.
 *
 * <p>This exception is thrown during OPA instance construction when plugins fail to initialize
 * within the expected timeframe or encounter errors during startup.
 *
 * <p>Common scenarios:
 *
 * <ul>
 *   <li>Bundle download failures
 *   <li>Discovery plugin configuration errors
 *   <li>Plugin timeout (not ready within expected timeframe)
 *   <li>Plugin status changes to ERROR during initialization
 * </ul>
 */
public class PluginInitializationException extends OpaException {

  /**
   * Create a PluginInitializationException with a message.
   *
   * @param message description of the initialization failure
   */
  public PluginInitializationException(String message) {
    super("plugin_init_error", message, null);
  }

  /**
   * Create a PluginInitializationException with a message and cause.
   *
   * @param message description of the initialization failure
   * @param cause the underlying exception that caused the initialization to fail
   */
  public PluginInitializationException(String message, Throwable cause) {
    super("plugin_init_error", message, cause);
  }
}
