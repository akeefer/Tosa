package tosa.api;

import tosa.DBConnection;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

/**
 * This interface represents a particular database that the application has access to.  From here,
 * metadata can be retrieved about the database tables and columns, as determined by the parsing
 * of the associated DDL file, and actual database operations can be performed.
 *
 * ${License}
 */
public interface IDatabase {

  // TODO - AHK - DB type, version, connection, etc.

  // TODO - AHK - This should be an interface to something in the API Package
  DBConnection getConnection();

  /**
   * Retrieves the associated table object, or null if no such table is found.  Table names
   * are case-sensitive.
   *
   * @param tableName the name of the table
   * @return the appropriate IDBTable, or null if the name doesn't match any table
   */
  IDBTable getTable(String tableName);

  /**
   * Returns all tables in this database, as determined by the CREATE TABLE statements in the
   * associated .ddl file.  This collection is immutable.
   *
   * @return all tables in this database
   */
  Collection<? extends IDBTable> getAllTables();

  /**
   * Returns the namespace in which the associated table objects live.  For example,
   * if the associated .ddl file is located in src/my/test/db.ddl, the namespace
   * returned for the corresponding IDatabase will be "my.test.db".
   *
   * @return the namespace associated with this database and its types
   */
  String getNamespace();

  // Query Execution Statements

  // TODO - AHK - Decide if this is really a good API

  IPreparedStatementParameter wrapParameter(Object value, IDBColumn column);

  // TODO - AHK - This should probably return more than just one object
  Object executeInsert(String sql, IPreparedStatementParameter... arguments);

  void executeUpdate(String sql, IPreparedStatementParameter... arguments);

  <T> List<T> executeSelect(String sql, IQueryResultProcessor<T> resultProcessor, IPreparedStatementParameter... arguments);

  void executeDelete(String sql, IPreparedStatementParameter... arguments);

  // TODO - AHK - Get CREATE TABLE statements
}
