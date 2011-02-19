package tosa.api;

import java.util.Collection;

/**
 * This interface represents a particular database that the application has access to.  From here,
 * metadata can be retrieved about the database tables and columns, as determined by the parsing
 * of the associated DDL file, and actual database operations can be performed.
 *
 * ${License}
 */
public interface IDatabase {

  /**
   * Returns the IDBConnection object associated with this database.
   *
   * @return the IDBConnection object for this database
   */
  IDBConnection getConnection();

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

  /**
   * Returns the IDBExecutionKernel object associated with this database.
   *
   * @return the IDBExecutionKernel object for this database
   */
  IDBExecutionKernel getDBExecutionKernel();

  // TODO - AHK - Remove this from IDatabase and move it to IDBColumn or IDBColumnType
  IPreparedStatementParameter wrapParameter(Object value, IDBColumn column);

  // TODO - AHK - Get CREATE TABLE statements

}
