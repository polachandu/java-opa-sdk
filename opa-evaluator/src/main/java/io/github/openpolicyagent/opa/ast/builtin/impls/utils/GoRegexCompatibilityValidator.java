package io.github.openpolicyagent.opa.ast.builtin.impls.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Validates Java regex patterns for Go compatibility. Go uses the RE2 engine which does not support
 * certain features.
 */
public class GoRegexCompatibilityValidator {

  /** Check if a Java regex pattern is compatible with Go's RE2 engine */
  public static ValidationResult validatePattern(String pattern) {
    // First validate Java syntax
    try {
      Pattern.compile(pattern);
    } catch (PatternSyntaxException e) {
      List<String> issues = new ArrayList<>();
      issues.add("Invalid Java regex syntax: " + e.getMessage());
      return new ValidationResult(false, issues, "Fix the Java regex syntax first");
    }

    List<String> incompatibilities = new ArrayList<>();

    // Check for backreferences
    if (hasBackreferences(pattern)) {
      incompatibilities.add("Backreferences (\\1, \\2, etc.) are not supported in Go");
    }

    // Check for lookahead
    if (hasLookahead(pattern)) {
      incompatibilities.add("Lookahead assertions ((?=...), (?!...)) are not supported in Go");
    }

    // Check for lookbehind
    if (hasLookbehind(pattern)) {
      incompatibilities.add("Lookbehind assertions ((?<=...), (?<!...)) are not supported in Go");
    }

    // Check for Unicode property classes
    if (hasUnicodeProperties(pattern)) {
      incompatibilities.add(
          "Unicode property classes (\\p{...}, \\P{...}) are not supported in Go");
    }

    // Check for conditional patterns
    if (hasConditionalPatterns(pattern)) {
      incompatibilities.add("Conditional patterns (?(...)...|...) are not supported in Go");
    }

    if (incompatibilities.isEmpty()) {
      return new ValidationResult(true, new ArrayList<>(), "");
    } else {
      String suggestion = generateSuggestion(incompatibilities);
      return new ValidationResult(false, incompatibilities, suggestion);
    }
  }

  /** Detect backreferences like \1, \2, \3, etc. */
  private static boolean hasBackreferences(String pattern) {
    // Match \1 through \9 (and \0 if not part of octal escape)
    // Look for \ followed by a digit, but not in a character class
    return pattern.matches("(?s).*(?<!\\\\)\\\\[1-9].*")
        || pattern.matches("(?s).*(?<!\\[)\\\\[1-9](?!]).*");
  }

  /** Detect positive and negative lookahead: (?=...) and (?!...) */
  private static boolean hasLookahead(String pattern) {
    return pattern.contains("(?=") || pattern.contains("(?!");
  }

  /** Detect positive and negative lookbehind: (?<=...) and (?<!...) */
  private static boolean hasLookbehind(String pattern) {
    return pattern.contains("(?<=") || pattern.contains("(?<!");
  }

  /** Detect Unicode property classes: \p{...} and \P{...} */
  private static boolean hasUnicodeProperties(String pattern) {
    return pattern.matches("(?s).*\\\\[pP]\\{[^}]+\\}.*");
  }

  /** Detect conditional patterns: (?(...)...|...) */
  private static boolean hasConditionalPatterns(String pattern) {
    return pattern.matches("(?s).*\\(\\?\\([^)]+\\)[^)]*\\|[^)]*\\).*");
  }

  /** Generate a helpful suggestion based on incompatibilities */
  private static String generateSuggestion(List<String> incompatibilities) {
    if (incompatibilities.isEmpty()) {
      return "";
    }

    StringBuilder suggestion = new StringBuilder();

    if (incompatibilities.stream().anyMatch(s -> s.contains("Backreferences"))) {
      suggestion.append("Backreferences: Use capture groups with manual post-processing instead. ");
    }

    if (incompatibilities.stream()
        .anyMatch(s -> s.contains("Lookahead") || s.contains("Lookbehind"))) {
      suggestion.append("Assertions: Replace with capture groups and extract what you need. ");
    }

    if (incompatibilities.stream().anyMatch(s -> s.contains("Unicode property"))) {
      suggestion.append("Unicode: Use explicit character ranges [α-ω] instead of \\p{Greek}. ");
    }

    if (incompatibilities.stream().anyMatch(s -> s.contains("Conditional"))) {
      suggestion.append("Conditionals: Use alternation with more specific patterns. ");
    }

    return suggestion.toString();
  }

  /** Quick check - returns true if compatible */
  public static boolean isGoCompatible(String pattern) {
    return validatePattern(pattern).isCompatible;
  }

  /** Result object for validation */
  public static class ValidationResult {
    public final boolean isCompatible;
    public final List<String> incompatibilities;
    public final String suggestion;

    public ValidationResult(
        boolean isCompatible, List<String> incompatibilities, String suggestion) {
      this.isCompatible = isCompatible;
      this.incompatibilities = incompatibilities;
      this.suggestion = suggestion;
    }

    @Override
    public String toString() {
      if (isCompatible) {
        return "✓ Pattern is Go-compatible!";
      }
      StringBuilder sb = new StringBuilder("✗ Pattern is NOT Go-compatible:\n");
      for (String issue : incompatibilities) {
        sb.append("  - ").append(issue).append("\n");
      }
      sb.append("Suggestion: ").append(suggestion);
      return sb.toString();
    }
  }
}
