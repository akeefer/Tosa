package tosa.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/10/11
 * Time: 10:28 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IPreparedStatementParameter {
  // TODO - AHK - Come up with a better name for this class
  // TODO - AHK - Should this be an inner interface somewhere?

  void setParameter(PreparedStatement statement, int index) throws SQLException;
}
