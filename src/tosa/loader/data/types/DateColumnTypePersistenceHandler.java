package tosa.loader.data.types;

import tosa.loader.data.IDBColumnTypePersistenceHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/19/11
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateColumnTypePersistenceHandler implements IDBColumnTypePersistenceHandler {

  @Override
  public Object readFromResultSet(ResultSet resultSet, String name) throws SQLException {
    java.sql.Date dbDate = resultSet.getDate(name);
    if (dbDate != null) {
      return new java.util.Date(dbDate.getTime());
    } else {
      return null;
    }
  }

  @Override
  public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
    if (value == null) {
      statement.setNull(index, Types.DATE);
    } else {
      if (!(value instanceof java.util.Date)) {
        throw new IllegalArgumentException("The DateColumnTypePersistenceHandler should not be used with an argument that's not of type java.util.Date");
      }
      statement.setDate(index, new java.sql.Date(((java.util.Date) value).getTime()));
    }
  }
}
