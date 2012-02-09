package tosa.db.execution;

import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.impl.RuntimeBridge;
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
    IDBObject obj = RuntimeBridge.createDBObject(type, false);
    IDBTable table = type.getTable();
    for (IDBColumn column : table.getColumns()) {
      Object resultObject = column.getColumnType().readFromResultSet(resultSet, table.getName() + "." + column.getName());
      obj.setColumnValue(column.getName(), resultObject);
    }
    return obj;
  }
}
