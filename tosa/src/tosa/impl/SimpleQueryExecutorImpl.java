package tosa.impl;

import org.slf4j.profiler.Profiler;
import tosa.CachedDBObject;
import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.api.IPreparedStatementParameter;
import tosa.api.IQueryResultProcessor;
import tosa.loader.IDBType;
import tosa.loader.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/8/11
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleQueryExecutorImpl implements SimpleQueryExecutor {

  // TODO - AHK - Clean up the query execution API for reals . . .

  @Override
  public int countWhere(String profilerTag, IDBType targetType, String whereClause, IPreparedStatementParameter... parameters) {
    // TODO - AHK - Quoting
    String fullQuery = "SELECT count(*) as count FROM \"" + targetType.getTable().getName() + "\" WHERE " + whereClause;
    return count(profilerTag, targetType, fullQuery, parameters);
  }

  @Override
  public int count(String profilerTag, IDBType targetType, String sqlStatement, IPreparedStatementParameter... parameters) {
    // TODO - AHK - Verify that it starts with "SELECT count(*) as count"
    Profiler profiler = Util.newProfiler(profilerTag);
    profiler.start(sqlStatement + " (" + Arrays.asList(parameters) + ")");
    try {
      List<Integer> results = targetType.getTable().getDatabase().getDBExecutionKernel().executeSelect(
          sqlStatement,
          new CountQueryResultProcessor(),
          parameters);
      if (results.size() == 0) {
        return 0;
      } else if (results.size() == 1) {
        return results.get(0);
      } else {
        throw new IllegalStateException("Expected count query " + sqlStatement + " to return 0 or 1 result, but got " + results.size());
      }
    } finally {
      profiler.stop();
    }
  }

  private static class CountQueryResultProcessor implements IQueryResultProcessor<Integer> {
    @Override
    public Integer processResult(ResultSet result) throws SQLException {
      return result.getInt("count");
    }
  }

  @Override
  public List<IDBObject> find(String profilerTag, IDBType type, String sqlStatement, IPreparedStatementParameter... parameters) {
    // TODO - AHK - Ensure that it starts with SELECT * ?
    Profiler profiler = Util.newProfiler(profilerTag);
    profiler.start(sqlStatement + " (" + Arrays.asList(sqlStatement) + ")");
    try {
      return type.getTable().getDatabase().getDBExecutionKernel().executeSelect(sqlStatement,
          new CachedDBQueryResultProcessor(type),
          parameters);
    } finally {
      profiler.stop();
    }
  }

  @Override
  public void update(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters) {
    // TODO - AHK
  }

  // TODO - AHK - This is a duplicate AND it's public
  // TODO - AHK The general query execution API here just needs a weeeee bit of help
  public static class CachedDBQueryResultProcessor implements IQueryResultProcessor<IDBObject> {
    private IDBType _type;

    public CachedDBQueryResultProcessor(IDBType type) {
      _type = type;
    }

    @Override
    public CachedDBObject processResult(ResultSet result) throws SQLException {
      return buildObject(_type, result);
    }
  }

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
