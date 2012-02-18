package tosa.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Callback interface for setting parameters on a prepared statement.
 */
public interface IPreparedStatementParameter {
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
