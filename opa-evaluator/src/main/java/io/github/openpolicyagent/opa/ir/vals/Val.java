package io.github.openpolicyagent.opa.ir.vals;

/**
 * Val represents an abstract value that statements operate on. There are currently 3 types of
 * values:
 *
 * <ol>
 *   <li>Local - a local variable that can refer to any type.
 *   <li>StringIndex - a string constant that refers to a compiled string.
 *   <li>Bool - a boolean constant.
 * </ol>
 */
public interface Val {
  String typeHint();
}
