package tosa.api;

import tosa.DBConnection;
import tosa.loader.data.DBData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

  // TODO - AHK - Remove this from IDatabase and move it to IDBColumn or IDBColumnType
  IPreparedStatementParameter wrapParameter(Object value, IDBColumn column);

  // TODO - AHK - This should probably return more than just one object
  Object executeInsert(String sql, IPreparedStatementParameter... arguments);

  <T> List<T> executeSelect(String sql, IQueryResultProcessor<T> resultProcessor, IPreparedStatementParameter... arguments);

  void executeUpdate(String sql, IPreparedStatementParameter... arguments);

  void executeDelete(String sql, IPreparedStatementParameter... arguments);

  // TODO - AHK - Get CREATE TABLE statements

  /**
   * Callback interface for setting parameters on a prepared statement.
   */
  interface IPreparedStatementParameter {
    /**
     * Given a PreparedStatement and an index into the prepared statement, set
     * the appropriate value into the PreparedStatement.
     *
     * @param statement the PreparedStatement to modify
     * @param index the index to use when setting a value into the PreparedStatement
     * @throws SQLException any SQLException (for purposes of allowing exceptions to propagate through)
     */
    void setParameter(PreparedStatement statement, int index) throws SQLException;
  }

  /**
   * Callback interface for processing query results.
   *
   * @param <T> the type of object returned from processing a single row within a ResultSet
   */
  interface IQueryResultProcessor<T> {

    /**
     * Processes a single row in a ResultSet to turn it into an appropriate object representation.  This method
     * should not do any operations that will alter the current row pointed to by the ResultSet.
     *
     * @param result the ResultSet
     * @return an object constructed from the current row of the ResultSet
     * @throws SQLException any SQLException (for purposes of allowing exceptions to propagate through)
     */
    T processResult(ResultSet result) throws SQLException;
  }
}
