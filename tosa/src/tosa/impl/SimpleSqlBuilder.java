package tosa.impl;

import tosa.api.IDBColumn;
import tosa.api.IDBTable;
import tosa.loader.IDBType;

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

}
