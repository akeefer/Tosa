package tosa.db.execution;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.features.PropertyReference;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuStringUtil;
import org.slf4j.profiler.Profiler;
import tosa.CachedDBObject;
import tosa.api.IDBColumn;
import tosa.api.IDBExecutionKernel;
import tosa.api.IDBTable;
import tosa.api.IDatabase;
import tosa.loader.DBPropertyInfo;
import tosa.loader.DBTypeInfo;
import tosa.loader.IDBType;
import tosa.loader.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
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

  public CachedDBObject selectById(String feature, IDBType type, Object id) throws SQLException {
    // TODO - AHK - Input validation (i.e. id should not be null)
    IDBTable table = type.getTable();
    IDBColumn idColumn = table.getColumn(DBTypeInfo.ID_COLUMN);
    IDatabase db = table.getDatabase();

    // TODO - AHK - Use some DB-aware utility to decide when to quote things, etc.
    // TODO - AHK - Make the column name a constant
    String query = "select * from \"" + table.getName() + "\" where \"id\" = ?";
    Profiler profiler = Util.newProfiler(feature);
    profiler.start(query + " (" + id + ")");
    List<CachedDBObject> results = db.getDBExecutionKernel().executeSelect(query,
        new CachedDBQueryResultProcessor(type),
        db.wrapParameter(id, idColumn));

    if (results.size() == 0) {
      return null;
    } else if (results.size() == 1) {
      return results.get(0);
    } else {
      throw new IllegalStateException("More than one row in table " + table.getName() + " had id " + id);
    }
  }

  public List<CachedDBObject> findFromTemplate(String feature, IDBType type, CachedDBObject template, PropertyReference sortColumn, boolean ascending, int limit, int offset) throws SQLException {
    IDBTable table = type.getTable();

    StringBuilder query = new StringBuilder("select * from \"");
    query.append(table.getName()).append("\" where ");
    List<IDBExecutionKernel.IPreparedStatementParameter> queryParameters = new ArrayList<IDBExecutionKernel.IPreparedStatementParameter>();
    addWhereClause(query, template, table, queryParameters);
    if (sortColumn != null) {
      query.append(" order by \"").append(sortColumn.getPropertyInfo().getName()).append("\" ").append(ascending ? "ASC" : "DESC").append(", \"id\" ASC");
    } else {
      query.append(" order by \"id\" ASC");
    }
    if (limit != -1) {
      query.append(" limit ").append(limit).append(" offset ").append(offset);
    }

    return findFromSql(feature, type, query.toString(), queryParameters);
  }

  public List<CachedDBObject> findFromSql(String feature, IDBType type, String query, List<IDBExecutionKernel.IPreparedStatementParameter> queryParameters) throws SQLException {
    Profiler profiler = Util.newProfiler(feature);
    profiler.start(query + " (" + queryParameters + ")");
    try {
      return type.getTable().getDatabase().getDBExecutionKernel().executeSelect(query,
          new CachedDBQueryResultProcessor(type),
          queryParameters.toArray(new IDBExecutionKernel.IPreparedStatementParameter[queryParameters.size()]));
    } finally {
      profiler.stop();
    }
  }

  public int countFromTemplate(String feature, IDBType type, CachedDBObject template) throws SQLException {
    IDBTable table = type.getTable();
    StringBuilder query = new StringBuilder("select count(*) as count from \"").append(table.getName()).append("\" where ");
    List<IDBExecutionKernel.IPreparedStatementParameter> queryParameters = new ArrayList<IDBExecutionKernel.IPreparedStatementParameter>();
    addWhereClause(query, template, table, queryParameters);
    return countFromSql(feature, type, query.toString(), queryParameters);
  }

  public int countFromSql(String feature, IDBType type, String query, List<IDBExecutionKernel.IPreparedStatementParameter> queryParameters) throws SQLException {
    Profiler profiler = Util.newProfiler(feature);
    profiler.start(query + " (" + queryParameters + ")");
    try {
      List<Integer> results = type.getTable().getDatabase().getDBExecutionKernel().executeSelect(query,
          new CountQueryResultProcessor(),
          queryParameters.toArray(new IDBExecutionKernel.IPreparedStatementParameter[queryParameters.size()]));
      if (results.size() == 0) {
        return 0;
      } else if (results.size() == 1) {
        return results.get(0);
      } else {
        throw new IllegalStateException("Expected count query " + query + " to return 0 or 1 result, but got " + results.size());
      }
    } finally {
      profiler.stop();
    }
  }

  private void addWhereClause(StringBuilder query, CachedDBObject template, IDBTable table, List<IDBExecutionKernel.IPreparedStatementParameter> parameters) {
    List<String> whereClause = new ArrayList<String>();
    if (template != null) {
      for (Map.Entry<String, Object> column : template.getColumns().entrySet()) {
        if (column.getValue() != null) {
          whereClause.add("\"" + column.getKey() + "\" = ?");
          parameters.add(table.getDatabase().wrapParameter(column.getValue(), table.getColumn(column.getKey())));
        }
      }
      if (!whereClause.isEmpty()) {
        query.append(GosuStringUtil.join(whereClause, " and "));
      } else {
        query.append("true");
      }
    } else {
      query.append("true");
    }
  }

  private static class CountQueryResultProcessor implements IDBExecutionKernel.IQueryResultProcessor<Integer> {
    @Override
    public Integer processResult(ResultSet result) throws SQLException {
      return result.getInt("count");
    }
  }

  // TODO - AHK - I don't really like having this be public
  public static class CachedDBQueryResultProcessor implements IDBExecutionKernel.IQueryResultProcessor<CachedDBObject> {
    private IDBType _type;

    public CachedDBQueryResultProcessor(IDBType type) {
      _type = type;
    }

    @Override
    public CachedDBObject processResult(ResultSet result) throws SQLException {
      return buildObject(_type, result);
    }
  }

  private static CachedDBObject buildObject(IDBType objectType, ResultSet result) throws SQLException {
    CachedDBObject obj = new CachedDBObject(objectType, false);
    // TODO - AHK - This should probably just iterate over columns rather than properties
    for (IPropertyInfo prop : objectType.getTypeInfo().getProperties()) {
      if (prop instanceof DBPropertyInfo) {
        DBPropertyInfo dbProp = (DBPropertyInfo) prop;
        // TODO - AHK - This should get the IDBColumn off the property instead of having a getColumnName method on it
        Object resultObject = result.getObject(objectType.getTable().getName() + "." + dbProp.getColumnName());
        if (resultObject instanceof BufferedReader) {
          obj.getColumns().put(dbProp.getColumnName(), readAll((BufferedReader) resultObject));
        } else if (resultObject instanceof Clob) {
          obj.getColumns().put(dbProp.getColumnName(), readAll(new BufferedReader(((Clob) resultObject).getCharacterStream())));
        } else if (dbProp.getFeatureType().equals(IJavaType.pBOOLEAN) && resultObject == null) {
          obj.getColumns().put(dbProp.getColumnName(), Boolean.FALSE);
        } else {
          obj.getColumns().put(dbProp.getColumnName(), resultObject);
        }
      }
    }
    return obj;
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
