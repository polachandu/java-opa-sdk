package io.github.open_policy_agent.opa;

import java.util.HashMap;
import java.util.Map;

public class OpaException extends RuntimeException {
  private final String errorCode;
  private final transient Map<String, Object> context;

  protected OpaException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.context = new HashMap<>();
  }

  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String getMessage() {
    String msg = super.getMessage();
    if (errorCode != null && msg != null && !msg.isEmpty()) {
      return errorCode + ": " + msg;
    }
    return msg;
  }

  public OpaException withContext(String key, Object value) {
    context.put(key, value);
    return this;
  }

  public Map<String, Object> getContext() {
    return context;
  }
}
