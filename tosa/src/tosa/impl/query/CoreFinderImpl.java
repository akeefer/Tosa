package tosa.impl.query;

import com.sun.org.apache.xpath.internal.operations.NotEquals;
import gw.lang.reflect.features.PropertyReference;
import gw.util.GosuStringUtil;
import org.slf4j.profiler.Profiler;
import tosa.CachedDBObject;
import tosa.api.*;
import tosa.impl.QueryExecutor;
import tosa.impl.QueryExecutorImpl;
import tosa.impl.SimpleSqlBuilder;
import tosa.loader.DBTypeInfo;
import tosa.loader.IDBType;
import tosa.loader.Util;
import tosa.loader.parser.tree.WhereClause;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/19/11
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoreFinderImpl {

  private QueryExecutor _queryExecutor;
  private IDBType _type;

  public CoreFinderImpl(IDBType type) {
    _type = type;
    _queryExecutor = new QueryExecutorImpl(type.getTable().getDatabase());
  }

  public IDBObject fromId(long id) throws SQLException {
    IDBTable table = _type.getTable();
    IDBColumn idColumn = table.getColumn(DBTypeInfo.ID_COLUMN);
    String query = SimpleSqlBuilder.substitute(
        "SELECT * FROM ${table} WHERE ${idColumn} = ?",
        "table", _type.getTable(),
        "idColumn", idColumn);
    List<IDBObject> results = _queryExecutor.selectEntity(_type.getName() + ".fromId()", _type, query, idColumn.wrapParameterValue(id));

    if (results.size() == 0) {
      return null;
    } else if (results.size() == 1) {
      return results.get(0);
    } else {
      throw new IllegalStateException("More than one row in table " + table.getName() + " had id " + id);
    }
  }

  // TODO - AHK - Make this return a long
  public int countWithSql(String sql) {
    return _queryExecutor.count(_type.getName() + ".countWithSql()", sql);
  }

  // TODO - AHK - Make this return a long
  public int count(IDBObject template) {
    // TODO - AHK - Validate that the template object is of the correct type
    String queryStart = SimpleSqlBuilder.substitute(
        "SELECT count(*) as count FROM ${table} WHERE ",
        "table", _type.getTable());
    Pair<String, List<IPreparedStatementParameter>> whereClauseAndParameters = buildWhereClauseForTemplate(template);
    String query = queryStart + whereClauseAndParameters.getFirst();
    IPreparedStatementParameter[] parameters = whereClauseAndParameters.getSecond().toArray(new IPreparedStatementParameter[whereClauseAndParameters.getSecond().size()]);
    return _queryExecutor.count(_type.getName() + ".count()", query, parameters);
  }

  public List<IDBObject> findWithSql(String sql) {
    return _queryExecutor.selectEntity(_type.getName() + ".findWithSql()", _type, sql);
  }

  public List<IDBObject> find(IDBObject template) {
    Pair<String, List<IPreparedStatementParameter>> whereClauseAndParameters = buildWhereClauseForTemplate(template);
    String query = SimpleSqlBuilder.substitute(
        "SELECT * FROM ${table} WHERE ${whereClause} ORDER BY ${idColumn} ASC",
        "table", _type.getTable(),
        "whereClause", whereClauseAndParameters.getFirst(),
        "idColumn", _type.getTable().getColumn(DBTypeInfo.ID_COLUMN));
    IPreparedStatementParameter[] parameters = whereClauseAndParameters.getSecond().toArray(new IPreparedStatementParameter[whereClauseAndParameters.getSecond().size()]);
    return _queryExecutor.selectEntity(_type.getName() + ".find()", _type, query, parameters);
  }

  public List<IDBObject> findSorted(IDBObject template, PropertyReference sortColumn, boolean ascending) {
    // TODO - AHK - Make sure that sortColumn is non-null
    Pair<String, List<IPreparedStatementParameter>> whereClauseAndParameters = buildWhereClauseForTemplate(template);
    String query = SimpleSqlBuilder.substitute(
        "SELECT * FROM ${table} WHERE ${whereClause} ORDER BY ${sortColumn} ${sortDirection}, ${idColumn} ASC",
        "table", _type.getTable(),
        "whereClause", whereClauseAndParameters.getFirst(),
        "sortColumn", _type.getTable().getColumn(sortColumn.getPropertyInfo().getName()),
        "sortDirection", ascending ? "ASC" : "DESC",
        "idColumn", _type.getTable().getColumn(DBTypeInfo.ID_COLUMN));
    IPreparedStatementParameter[] parameters = whereClauseAndParameters.getSecond().toArray(new IPreparedStatementParameter[whereClauseAndParameters.getSecond().size()]);
    return _queryExecutor.selectEntity(_type.getName() + ".findSorted()", _type, query, parameters);
  }

  public List<IDBObject> findPaged(IDBObject template, int pageSize, int offset) {
    // TODO - AHK - Is the parameter value wrapping there acceptable?
    Pair<String, List<IPreparedStatementParameter>> whereClauseAndParameters = buildWhereClauseForTemplate(template);
    IDBColumn idColumn = _type.getTable().getColumn(DBTypeInfo.ID_COLUMN);
    String query = SimpleSqlBuilder.substitute(
        "SELECT * FROM ${table} WHERE ${whereClause} ORDER BY ${idColumn} ASC LIMIT ? OFFSET ?",
        "table", _type.getTable(),
        "whereClause", whereClauseAndParameters.getFirst(),
        "idColumn", idColumn);
    List<IPreparedStatementParameter> parameterList = new ArrayList<IPreparedStatementParameter>(whereClauseAndParameters.getSecond());
    parameterList.add(idColumn.wrapParameterValue(pageSize));
    parameterList.add(idColumn.wrapParameterValue(offset));
    IPreparedStatementParameter[] parameters = parameterList.toArray(new IPreparedStatementParameter[parameterList.size()]);
    return _queryExecutor.selectEntity(_type.getName() + ".findPaged()", _type, query, parameters);
  }

  public List<IDBObject> findSortedPaged(IDBObject template, PropertyReference sortColumn, boolean ascending,  int pageSize, int offset) {
    // TODO - AHK - Is the parameter value wrapping there acceptable?
    Pair<String, List<IPreparedStatementParameter>> whereClauseAndParameters = buildWhereClauseForTemplate(template);
    IDBColumn idColumn = _type.getTable().getColumn(DBTypeInfo.ID_COLUMN);
    String query = SimpleSqlBuilder.substitute(
        "SELECT * FROM ${table} WHERE ${whereClause} ORDER BY ${sortColumn} ${sortDirection}, ${idColumn} ASC LIMIT ? OFFSET ?",
        "table", _type.getTable(),
        "whereClause", whereClauseAndParameters.getFirst(),
        "sortColumn", _type.getTable().getColumn(sortColumn.getPropertyInfo().getName()),
        "sortDirection", ascending ? "ASC" : "DESC",
        "idColumn", idColumn);
    List<IPreparedStatementParameter> parameterList = new ArrayList<IPreparedStatementParameter>(whereClauseAndParameters.getSecond());
    parameterList.add(idColumn.wrapParameterValue(pageSize));
    parameterList.add(idColumn.wrapParameterValue(offset));
    IPreparedStatementParameter[] parameters = parameterList.toArray(new IPreparedStatementParameter[parameterList.size()]);
    return _queryExecutor.selectEntity(_type.getName() + ".findSortedPaged()", _type, query, parameters);
  }

  private Pair<String, List<IPreparedStatementParameter>> buildWhereClauseForTemplate(IDBObject template) {
    List<String> whereClause = new ArrayList<String>();
    List<IPreparedStatementParameter> parameters = new ArrayList<IPreparedStatementParameter>();
    if (template != null) {
      for (IDBColumn column : _type.getTable().getColumns()) {
        // TODO - AHK - Use a variety that takes an IDBColumn as an argument here
        Object value = template.getColumnValue(column.getName());
        if (value != null) {
          whereClause.add(SimpleSqlBuilder.substitute("${column} = ?", "column", column));
          parameters.add(column.wrapParameterValue(value));
        }
      }

      if (!whereClause.isEmpty()) {
        return new Pair<String, List<IPreparedStatementParameter>>(GosuStringUtil.join(whereClause, " AND "), parameters);
      } else {
        return new Pair<String, List<IPreparedStatementParameter>>("true", parameters);
      }
    } else {
      return new Pair<String, List<IPreparedStatementParameter>>("true", parameters);
    }
  }

  private static class Pair<A, B> {
    private A _first;
    private B _second;

    private Pair(A first, B second) {
      _first = first;
      _second = second;
    }

    public A getFirst() {
      return _first;
    }

    public B getSecond() {
      return _second;
    }
  }
}
