package tosa.loader.data.types;

import tosa.loader.data.IDBColumnTypePersistenceHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/19/11
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenericDBColumnTypePersistenceHandler implements IDBColumnTypePersistenceHandler {

  private int _jdbcType;

  public GenericDBColumnTypePersistenceHandler(int jdbcType) {
    _jdbcType = jdbcType;
  }

  @Override
  public Object readFromResultSet(ResultSet result, String name) throws SQLException {
    Object resultObject = result.getObject(name);
    if (resultObject instanceof BufferedReader) {
      return readAll((BufferedReader) resultObject);
    } else if (resultObject instanceof Clob) {
      return readAll(new BufferedReader(((Clob) resultObject).getCharacterStream()));
    } else {
      return resultObject;
    }
  }

  @Override
  public void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
    // TODO - AHK - Lots of stuff here
    if (value == null) {
      statement.setNull(index, _jdbcType);
    } else {
      // TODO - AHK - Deal with lots of other potential types more specifically
      statement.setObject(index, value, _jdbcType);
    }
  }

  private static Object readAll(BufferedReader r) {
    try {
      StringBuilder b = new StringBuilder();
      String line = r.readLine();
      while (line != null) {
        b.append(line).append("\n");
        line = r.readLine();
      }
      if (b.length() > 0) {
        b.setLength(b.length() - 1);
      }
      return b.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
