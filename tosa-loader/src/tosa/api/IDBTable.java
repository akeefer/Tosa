package tosa.api;

import tosa.loader.parser.tree.SQLParseError;

import java.util.Collection;
import java.util.List;

/**
 * An interface that represents a database table.
 *
 * ${License}
 */
public interface IDBTable {

  // TODO - AHK - Other attributes

  /**
   * Returns the IDatabase that this table is a part of.
   *
   * @return the IDatabase that this table is a part of
   */
  IDatabase getDatabase();

  /**
   * Returns the name of this table, as defined in the CREATE TABLE statement.  Note that
   * this name is considered to be case-sensitive.
   *
   * @return the name of this table
   */
  String getName();

  /**
   * Returns the name of this table, surrounded by quotes if the original table name
   * in the DDL file was specified with quotes.
   *
   * @return the name of this table, surrounded by quotes if that's how it was specified
   *         in the DDL file
   */
  String getPossiblyQuotedName();

  /**
   * Returns the IDBColumn with the given case-sensitive name, if any such column exists on this table.
   * The name is considered to be case-sensitive.
   *
   * @param name the name of the column
   * @return the IDBColumn with exactly the given name, or null if there is no such column
   */
  IDBColumn getColumn(String name);

  /**
   * Returns a Collection containing all of the columns on this table.  The returned collection is unmodifiable.
   *
   * @return a Collection of all IDBColumns on this table
   */
  Collection<? extends IDBColumn> getColumns();

  // TODO - AHK - Do we really need this?
  Collection<? extends IDBColumn> getIncomingFKs();

  /**
   * Indicates whether or not this table has an id column.  As a general rule, only join tables are allowed
   * to not have an id column.
   *
   * @return true if this table has an id column, false otherwise
   */
  boolean hasId();

  IDBArray getArray(String propertyName);

  Collection<? extends IDBArray> getArrays();

  // TODO - AHK - Some attribute to indicate it's a join table?

  List<SQLParseError> getErrors();
}
