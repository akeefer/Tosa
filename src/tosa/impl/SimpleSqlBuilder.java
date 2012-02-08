package tosa.impl;

import tosa.api.IDBColumn;
import tosa.api.IDBTable;
import tosa.loader.IDBType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/13/11
 * Time: 11:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleSqlBuilder {

  public static String substitute(String sql, String name0, Object value0) {
    Map<String, Object> substitutionMap = new HashMap<String, Object>();
    substitutionMap.put(name0, value0);
    return substituteString(sql, substitutionMap);
  }

  public static String substitute(String sql, String name0, Object value0, String name1, Object value1) {
    Map<String, Object> substitutionMap = new HashMap<String, Object>();
    substitutionMap.put(name0, value0);
    substitutionMap.put(name1, value1);
    return substituteString(sql, substitutionMap);
  }

  public static String substitute(String sql, String name0, Object value0, String name1, Object value1, String name2, Object value2) {
    Map<String, Object> substitutionMap = new HashMap<String, Object>();
    substitutionMap.put(name0, value0);
    substitutionMap.put(name1, value1);
    substitutionMap.put(name2, value2);
    return substituteString(sql, substitutionMap);
  }

  public static String substitute(String sql, String name0, Object value0, String name1, Object value1, String name2, Object value2, String name3, Object value3) {
    Map<String, Object> substitutionMap = new HashMap<String, Object>();
    substitutionMap.put(name0, value0);
    substitutionMap.put(name1, value1);
    substitutionMap.put(name2, value2);
    substitutionMap.put(name3, value3);
    return substituteString(sql, substitutionMap);
  }

  public static String substitute(String sql, String name0, Object value0, String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4) {
    Map<String, Object> substitutionMap = new HashMap<String, Object>();
    substitutionMap.put(name0, value0);
    substitutionMap.put(name1, value1);
    substitutionMap.put(name2, value2);
    substitutionMap.put(name3, value3);
    substitutionMap.put(name4, value4);
    return substituteString(sql, substitutionMap);
  }

  private static String substituteString(String source, Map<String, Object> values) {
    StringBuilder result = new StringBuilder();
    boolean parsingToken = false;
    int tokenStart = 0;
    int i = 0;
    while (i < source.length()) {
      if (parsingToken) {
        if (source.charAt(i) == '}') {
          String tokenName = source.substring(tokenStart, i);
          if (!values.containsKey(tokenName)) {
            throw new IllegalArgumentException("No substitution value was found for the token " + tokenName + ".  Source was " + source + " and values are " + values);
          }
          Object newValue = values.get(tokenName);
          result.append(maybeQuote(newValue));
          parsingToken = false;
        }
        i++;
      } else {
        if (source.charAt(i) == '$' && i + 1 < source.length() && source.charAt(i + 1) == '{') {
          tokenStart = i + 2;
          parsingToken = true;
          i += 2;
        } else {
          result.append(source.charAt(i));
          i++;
        }
      }
    }

    return result.toString();
  }

  private static String maybeQuote(Object value) {
    if (value instanceof Iterable) {
      StringBuilder values = new StringBuilder();
      boolean first = true;
      for (Object v : ((Collection) value)) {
        if (first) {
          first = false;
        } else {
          values.append(", ");
        }
        values.append(maybeQuote(v));
      }
      return values.toString();
    } else if (value instanceof IDBColumn) {
      return ((IDBColumn) value).getPossiblyQuotedName();
    } else if (value instanceof IDBTable) {
      return ((IDBTable) value).getPossiblyQuotedName();
    } else {
      return value.toString();
    }
  }
}
