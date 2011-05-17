package tosa.impl;

import tosa.api.IDBColumn;
import tosa.api.IDBTable;
import tosa.loader.IDBType;

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

  private StringBuilder _text;

  public SimpleSqlBuilder() {
    _text = new StringBuilder();
  }

  public static SimpleSqlBuilder select(String text) {
    SimpleSqlBuilder builder = new SimpleSqlBuilder();
    builder._text.append("SELECT ");
    builder._text.append(text);
    builder._text.append(" ");
    return builder;
  }

  public static SimpleSqlBuilder select(IDBColumn... columns) {
    SimpleSqlBuilder builder = new SimpleSqlBuilder();
    builder._text.append("SELECT ");
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        builder._text.append(", ");
      }
      builder._text.append("\"").append(columns[i].getName()).append("\"");
    }
    builder._text.append(" ");
    return builder;
  }

  public static SimpleSqlBuilder update(IDBTable table) {
    SimpleSqlBuilder builder = new SimpleSqlBuilder();
    builder._text.append("UPDATE ").append("\"").append(table.getName()).append("\" ");
    return builder;
  }

  public SimpleSqlBuilder from(IDBTable table) {
    _text.append("FROM ").append("\"").append(table.getName()).append("\" ");
    return this;
  }

  public SimpleSqlBuilder from(IDBType type) {
    return from(type.getTable());
  }

  public SimpleSqlBuilder from(String text) {
    _text.append("FROM ").append(text).append(" ");
    return this;
  }

  public SimpleSqlBuilder set(IDBColumn column, String value) {
    _text.append("SET ").append("\"").append(column.getName()).append("\" = ").append(value).append(" ");
    return this;
  }

  public SimpleSqlBuilder where(String text) {
    _text.append("WHERE ").append(text);
    return this;
  }

  public SimpleSqlBuilder where(IDBColumn column, String op, String value) {
    _text.append("WHERE ").append("\"").append(column.getName()).append("\" ").append(op).append(" ").append(value).append(" ");
    return this;
  }

  public SimpleSqlBuilder order_by(IDBColumn column) {
    _text.append("ORDER BY \"").append(column.getName()).append("\"");
    return this;
  }

  public SimpleSqlBuilder append(String text) {
    _text.append(text);
    return this;
  }

  @Override
  public String toString() {
    // TODO - AHK - Trim any whitespace?
    // TODO - AHK - Disallow modifications after this point?
    return _text.toString();
  }

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
    if (value instanceof IDBColumn) {
      return "\"" + ((IDBColumn) value).getName() + "\"";
    } else if (value instanceof IDBTable) {
      return "\"" + ((IDBTable) value).getName() + "\"";
    } else {
      return value.toString();
    }
  }
}
