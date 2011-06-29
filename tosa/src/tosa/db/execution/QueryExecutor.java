package tosa.db.execution;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.features.PropertyReference;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuStringUtil;
import org.slf4j.profiler.Profiler;
import tosa.CachedDBObject;
import tosa.api.*;
import tosa.loader.DBPropertyInfo;
import tosa.loader.DBTypeInfo;
import tosa.loader.IDBType;
import tosa.loader.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

  public static CachedDBObject buildObject(IDBType type, ResultSet resultSet) throws SQLException {
    CachedDBObject obj = new CachedDBObject(type, false);
    IDBTable table = type.getTable();
    for (IDBColumn column : table.getColumns()) {
      Object resultObject = column.getColumnType().readFromResultSet(resultSet, table.getName() + "." + column.getName());
      obj.setColumnValue(column.getName(), resultObject);
    }
    return obj;
  }
}
