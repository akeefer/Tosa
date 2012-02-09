package tosa.db.execution;

import gw.lang.reflect.ReflectUtil;
import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.loader.IDBType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/11/11
 * Time: 9:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryExecutor {

  // TODO - AHK - Kill this/move it somewhere else

  public static IDBObject buildObject(IDBType type, ResultSet resultSet) throws SQLException {
    // TODO - AHK - This is cleeaarrrlly a hack
    IDBObject obj = ReflectUtil.construct("tosa.CachedDBObject", type, false);
    IDBTable table = type.getTable();
    for (IDBColumn column : table.getColumns()) {
      Object resultObject = column.getColumnType().readFromResultSet(resultSet, table.getName() + "." + column.getName());
      obj.setColumnValue(column.getName(), resultObject);
    }
    return obj;
  }
}
