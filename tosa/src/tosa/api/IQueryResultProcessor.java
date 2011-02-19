package tosa.api;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Callback interface for processing query results.
 *
 * @param <T> the type of object returned from processing a single row within a ResultSet
 */
public interface IQueryResultProcessor<T> {

  /**
   * Processes a single row in a ResultSet to turn it into an appropriate object representation.  This method
   * should not do any operations that will alter the current row pointed to by the ResultSet.
   *
   * @param result the ResultSet
   * @return an object constructed from the current row of the ResultSet
   * @throws java.sql.SQLException any SQLException (for purposes of allowing exceptions to propagate through)
   */
  T processResult(ResultSet result) throws SQLException;
}
