package io.github.openpolicyagent.opa.rego;

import io.github.openpolicyagent.opa.logging.Logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Hook interface for intercepting OPA print() builtin output.
 *
 * <p>When configured on {@link EvaluationContext}, the {@code internal.print} builtin will call
 * {@link #print(String)} with the formatted message. If no PrintHook is configured, print
 * statements are silently ignored (evaluation still succeeds).
 *
 * <p>This mirrors the Go OPA {@code print.Hook} interface from {@code topdown/print/print.go}.
 */
public interface PrintHook {

  /**
   * Called when a print() statement is evaluated.
   *
   * @param message the formatted print message (arguments joined by spaces)
   * @throws Exception if the hook encounters an error writing output
   */
  void print(String message) throws Exception;

  /**
   * Returns a PrintHook that writes to the given {@link Logger} at debug level.
   *
   * @param logger the logger to write print output to
   */
  static PrintHook logger(Logger logger) {
    return msg -> logger.debug(msg);
  }

  /**
   * Returns a PrintHook that writes each message as a line to the given {@link OutputStream}.
   *
   * <p>This mirrors Go OPA's {@code NewPrintHook(w io.Writer)} from {@code topdown/print.go}. The
   * caller retains ownership of the stream; this hook will not close it.
   *
   * @param out the output stream to write to
   */
  static PrintHook of(OutputStream out) {
    if (out instanceof PrintStream) {
      return ((PrintStream) out)::println;
    }
    return msg -> {
      byte[] bytes = (msg + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
      out.write(bytes);
      out.flush();
    };
  }
}
