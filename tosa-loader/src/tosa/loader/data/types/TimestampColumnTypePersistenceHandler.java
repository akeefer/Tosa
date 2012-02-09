package tosa.loader.data.types;

import tosa.loader.data.IDBColumnTypePersistenceHandler;

import java.sql.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/19/11
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimestampColumnTypePersistenceHandler implements IDBColumnTypePersistenceHandler {

  @Override
  public Object readFromResultSet(ResultSet resultSet, String name) throws SQLException {
    Timestamp dbDate = resultSet.getTimestamp(name);
    if (dbDate != null) {
      return new java.util.Date(dbDate.getTime());
    } else {
      return null;
    }
  }

  @Override
  public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
    if (value == null) {
      statement.setNull(index, Types.TIMESTAMP);
    } else {
      if (!(value instanceof java.util.Date)) {
        throw new IllegalArgumentException("The TimestampColumnTypePersistenceHandler should not be used with an argument that's not of type java.util.Date");
      }
      statement.setTimestamp(index, new java.sql.Timestamp(((java.util.Date) value).getTime()));
    }
  }
}
