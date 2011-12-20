package tosa.loader.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/19/11
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBColumnTypePersistenceHandler {

  Object readFromResultSet(ResultSet resultSet, String name) throws SQLException;

  void setParameter(PreparedStatement statement, int index, Object value) throws SQLException;
}
