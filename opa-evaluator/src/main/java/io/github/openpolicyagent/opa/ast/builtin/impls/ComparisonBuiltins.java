package io.github.openpolicyagent.opa.ast.builtin.impls;

import java.util.Map;
import java.util.function.BiFunction;
import io.github.openpolicyagent.opa.ast.builtin.OpaBuiltin;
import io.github.openpolicyagent.opa.ast.builtin.OpaType;
import io.github.openpolicyagent.opa.ast.types.RegoBoolean;
import io.github.openpolicyagent.opa.ast.types.RegoValue;
import io.github.openpolicyagent.opa.rego.EvaluationContext;

public class ComparisonBuiltins {
  public static Map<String, BiFunction<EvaluationContext, RegoValue[], RegoValue>> builtins() {
    ComparisonBuiltins instance = new ComparisonBuiltins();
    return Map.of(
        "gte", instance::gte,
        "gt", instance::gt,
        "lt", instance::lt,
        "lte", instance::lte,
        "neq", instance::neq,
        "equal", instance::equal);
    }

  @OpaBuiltin(
      name = "gt",
      categories = {"comparison"},
      args = {@OpaType(type = "any", name = "x"), @OpaType(type = "any", name = "y")},
      result =
          @OpaType(
              type = "boolean",
              name = "result",
              description = "true if `x` is greater than `y`; false otherwise"),
      infix = ">")
  public RegoValue gt(EvaluationContext ctx, RegoValue[] args) {

        RegoValue x = args[0];
        RegoValue y = args[1];

        return x.compareTo(y) > 0 ?RegoBoolean.TRUE:RegoBoolean.FALSE;
    }

  @OpaBuiltin(
      name = "gte",
      categories = {"comparison"},
      args = {@OpaType(type = "any", name = "x"), @OpaType(type = "any", name = "y")},
      result =
          @OpaType(
              type = "boolean",
              name = "result",
              description = "true if `x` is greater or equal to `y`; false otherwise"),
      infix = ">=")
  public RegoValue gte(EvaluationContext ctx, RegoValue[] args) {

        RegoValue x = args[0];
        RegoValue y = args[1];

        return x.compareTo(y) >= 0?RegoBoolean.TRUE:RegoBoolean.FALSE;
    }

  @OpaBuiltin(
      name = "lt",
      categories = {"comparison"},
      args = {@OpaType(type = "any", name = "x"), @OpaType(type = "any", name = "y")},
      result =
          @OpaType(
              type = "boolean",
              name = "result",
              description = "true if `x` is less than `y`; false otherwise"),
      infix = "<")
  public RegoValue lt(EvaluationContext ctx, RegoValue[] args) {

        RegoValue x = args[0];
        RegoValue y = args[1];

        return x.compareTo(y) < 0?RegoBoolean.TRUE:RegoBoolean.FALSE;
    }

  @OpaBuiltin(
      name = "lte",
      categories = {"comparison"},
      args = {@OpaType(type = "any", name = "x"), @OpaType(type = "any", name = "y")},
      result =
          @OpaType(
              type = "boolean",
              name = "result",
              description = "true if `x` is less or equal to `y`; false otherwise"),
      infix = "<=")
  public RegoValue lte(EvaluationContext ctx, RegoValue[] args) {

        RegoValue x = args[0];
        RegoValue y = args[1];

        return x.compareTo(y) <= 0?RegoBoolean.TRUE:RegoBoolean.FALSE;
    }

  @OpaBuiltin(
      name = "equal",
      categories = {"comparison"},
      args = {@OpaType(type = "any", name = "x"), @OpaType(type = "any", name = "y")},
      result =
          @OpaType(
              type = "boolean",
              name = "result",
              description = "true if `x` is equal to `y`; false otherwise"),
      infix = "==")
  public RegoValue equal(EvaluationContext ctx, RegoValue[] args) {

        RegoValue x = args[0];
        RegoValue y = args[1];

        return x.compareTo(y) == 0?RegoBoolean.TRUE:RegoBoolean.FALSE;
    }

  @OpaBuiltin(
      name = "neq",
      categories = {"comparison"},
      args = {@OpaType(type = "any", name = "x"), @OpaType(type = "any", name = "y")},
      result =
          @OpaType(type = "boolean", description = "true if x is not equal to y; false otherwise"),
      infix = "!=")
  public RegoValue neq(EvaluationContext ctx, RegoValue[] args) {

        RegoValue x = args[0];
        RegoValue y = args[1];

        return x.compareTo(y) != 0?RegoBoolean.TRUE:RegoBoolean.FALSE;
    }
}