package io.github.openpolicyagent.opa.ast.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for deep comparison of composite types (arrays, sets, objects) in composite
 * reference lookups.
 *
 * <p>This class provides methods to check if a collection contains a specific array, set, or object
 * by comparing their values rather than relying on object equality.
 */
public class CompositeReferenceUtil {

  /**
   * Check if any array in the collection matches the search array by comparing elements in order.
   *
   * @param collection the collection to search in
   * @param searchArray the array to search for
   * @return true if a matching array is found
   */
  public static boolean containsArray(Iterable<RegoValue> collection, RegoArray searchArray) {
    List<RegoValue> searchValues = searchArray.getValue();

    for (RegoValue candidate : collection) {
      if (candidate instanceof RegoArray) {
        RegoArray candidateArray = (RegoArray) candidate;
        List<RegoValue> candidateValues = candidateArray.getValue();

        // Check if both arrays have the same size and elements in the same order
        if (candidateValues.size() == searchValues.size()) {
          boolean match = true;
          for (int i = 0; i < candidateValues.size(); i++) {
            if (!candidateValues.get(i).equals(searchValues.get(i))) {
              match = false;
              break;
            }
          }
          if (match) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Check if any set in the collection matches the search set by comparing all values.
   *
   * @param collection the collection to search in
   * @param searchSet the set to search for
   * @return true if a matching set is found
   */
  public static boolean containsSet(Iterable<RegoValue> collection, RegoSet searchSet) {
    Set<RegoValue> searchValues = searchSet.getValue();

    for (RegoValue candidate : collection) {
      if (candidate instanceof RegoSet) {
        RegoSet candidateSet = (RegoSet) candidate;
        Set<RegoValue> candidateValues = candidateSet.getValue();

        // Check if both sets have the same size and all values exist in both
        if (candidateValues.size() == searchValues.size()
            && candidateValues.containsAll(searchValues)
            && searchValues.containsAll(candidateValues)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Check if any object in the collection matches the search object by comparing all properties.
   *
   * @param collection the collection to search in
   * @param searchObject the object to search for
   * @return true if a matching object is found
   */
  public static boolean containsObject(Iterable<RegoValue> collection, RegoObject searchObject) {
    Map<RegoValue, RegoValue> searchProps = searchObject.getProperties();

    for (RegoValue candidate : collection) {
      if (candidate instanceof RegoObject) {
        RegoObject candidateObject = (RegoObject) candidate;
        Map<RegoValue, RegoValue> candidateProps = candidateObject.getProperties();

        // Check if both objects have the same properties
        if (candidateProps.size() == searchProps.size()) {
          boolean match = true;
          for (Map.Entry<RegoValue, RegoValue> entry : searchProps.entrySet()) {
            RegoValue candidateValue = candidateProps.get(entry.getKey());
            if (candidateValue == null || !candidateValue.equals(entry.getValue())) {
              match = false;
              break;
            }
          }
          if (match) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
