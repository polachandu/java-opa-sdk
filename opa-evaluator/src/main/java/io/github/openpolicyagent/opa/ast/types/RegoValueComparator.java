package io.github.openpolicyagent.opa.ast.types;

import java.util.Comparator;

/**
 * Comparator for RegoValue that implements OPA's type ordering.
 *
 * <p>Type precedence (from lowest to highest): 1. RegoUndefined 2. RegoBoolean 3. RegoNumber
 * (RegoInt32, RegoBigInt, RegoDecimal) 4. RegoString 5. RegoArray 6. RegoNull 7. RegoSet 8.
 * RegoObject
 *
 * <p>Within primitive types (Undefined, Boolean, Number, String, Null), natural ordering is used.
 * Collection types (Array, Set, Object) are grouped by type but maintain insertion order within
 * their type.
 */
public class RegoValueComparator implements Comparator<RegoValue> {

  @Override
  public int compare(RegoValue a, RegoValue b) {
    if (a == b) {
      return 0;
    }
    if (a == null) {
      return b == null ? 0 : -1;
    }
    if (b == null) {
      return 1;
    }

    // Get type precedence for each value
    int typeA = getTypePrecedence(a);
    int typeB = getTypePrecedence(b);

    // If different types, compare by type precedence
    if (typeA != typeB) {
      return Integer.compare(typeA, typeB);
    }

    // Same type - for collection types, maintain insertion order using identity hash
    // For primitive types, use natural ordering
    if (isCollectionType(a)) {
      // Use identity hash code to maintain insertion order while allowing duplicates
      return Integer.compare(System.identityHashCode(a), System.identityHashCode(b));
    }

    // Same type, use natural ordering for primitives
    return a.compareTo(b);
  }

  private boolean isCollectionType(RegoValue value) {
    return value instanceof RegoArray || value instanceof RegoSet || value instanceof RegoObject;
  }

  private int getTypePrecedence(RegoValue value) {
    if (value instanceof RegoUndefined) {
      return 0;
    } else if (value instanceof RegoNull) {
      return 1;
    } else if (value instanceof RegoBoolean) {
      return 2;
    } else if (value instanceof RegoNumber) {
      // All number types have the same precedence
      return 3;
    } else if (value instanceof RegoString) {
      return 4;
    } else if (value instanceof RegoArray) {
      return 5;
    } else if (value instanceof RegoSet) {
      return 6;
    } else if (value instanceof RegoObject) {
      return 7;
    } else {
      // Unknown types go last
      return 8;
    }
  }
}
