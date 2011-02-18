package tosa.api;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/18/11
 * Time: 8:34 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBExecutionKernel {

  // TODO - AHK - Decide on packaging

  // TODO - AHK - Decide if this is really a good API

    // TODO - AHK - This should probably return more than just one object
  Object executeInsert(String sql, IPreparedStatementParameter... arguments);

  <T> List<T> executeSelect(String sql, IQueryResultProcessor<T> resultProcessor, IPreparedStatementParameter... arguments);

  void executeUpdate(String sql, IPreparedStatementParameter... arguments);

  void executeDelete(String sql, IPreparedStatementParameter... arguments);

  // Should these be moved out to top-level interfaces again?

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
     * @throws java.sql.SQLException any SQLException (for purposes of allowing exceptions to propagate through)
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
