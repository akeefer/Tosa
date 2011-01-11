package tosa.dbmd;

import tosa.api.IPreparedStatementParameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/10/11
 * Time: 10:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class PreparedStatementParameterImpl implements IPreparedStatementParameter {

  private Object _value;
  private int _jdbcType;

  public PreparedStatementParameterImpl(Object value, int jdbcType) {
    _value = value;
    _jdbcType = jdbcType;
  }

  @Override
  public void setParameter(PreparedStatement statement, int index) throws SQLException {
    // TODO - AHK - Lots of stuff here
    if (_value == null) {
      statement.setNull(index, _jdbcType);
    } else {
      // TODO - AHK - Deal with lots of other potential types more specifically
      statement.setObject(index, _value, _jdbcType);
    }
  }
}
