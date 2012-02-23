package tosa.db.execution;

import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.impl.RuntimeBridge;
import tosa.loader.IDBType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
    Map<String, Object> originalValues = new HashMap<String, Object>();
    IDBTable table = type.getTable();
    for (IDBColumn column : table.getColumns()) {
      Object resultObject = column.getColumnType().readFromResultSet(resultSet, table.getName() + "." + column.getName());
      originalValues.put(column.getName(), resultObject);
    }
    return RuntimeBridge.createDBObject(type, originalValues);
  }
}
