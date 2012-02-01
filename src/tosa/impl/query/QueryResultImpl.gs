package tosa.impl.query

uses tosa.api.QueryResult
uses tosa.api.IPreparedStatementParameter
uses tosa.api.IQueryResultProcessor
uses tosa.api.IDatabase
uses java.util.Iterator
uses tosa.loader.Util
uses java.util.Arrays
uses gw.lang.reflect.features.IPropertyReference
uses tosa.api.IDBObject
uses java.lang.StringBuilder
uses java.util.ArrayList

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 7/31/11
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryResultImpl<T> implements QueryResult<T> {

  var _profilerTag : String
  var _originalQuery : String
  var _parameters : IPreparedStatementParameter[]
  var _db : IDatabase
  var _resultProcessor : IQueryResultProcessor<T>

  // TODO - AHK - Paging, all that stuff
  var _results : List<T>
  var _orderBys : List<OrderByInfo>

  public construct(profilerTag : String, originalQuery : String, parameters : IPreparedStatementParameter[], db : IDatabase, resultProcessor : IQueryResultProcessor<T>) {
    _profilerTag = profilerTag
    _originalQuery = originalQuery
    _parameters = parameters
    _db = db
    _resultProcessor = resultProcessor
    _orderBys = new ArrayList<OrderByInfo>()
  }

  override property get Count() : int {
    maybeLoadResults()
    return _results.size()
  }

  override function size() : int {
    return Count
  }

  override function orderBySql(sql : String): QueryResult<T> {
    _orderBys.add(new OrderByInfo(sql))
    return this
  }

  override function orderBy(sortColumn : IPropertyReference<IDBObject, Object>, sortDirection: QueryResult.SortDirection): QueryResult<T> {
    _orderBys.add(new OrderByInfo(sortColumn, sortDirection))
    return this
  }

  override function iterator(): Iterator <T> {
    maybeLoadResults()
    return _results.iterator()
  }

  override function get(idx : int) : T {
    maybeLoadResults()
    return _results.get(idx)
  }

  private function maybeLoadResults() {
    if (_results == null) {
      _results = executeQuery()
    }
  }

  private function computeSql() : String {
    if (_orderBys.Empty) {
      return _originalQuery
    } else {
      return _originalQuery + buildOrderByClause()  
    }
  }
  
  private function buildOrderByClause() : String {
    var result = new StringBuilder()
    result.append(" ORDER BY ")
    result.append(_orderBys.map( \ o -> o.asSql()).join(", "))
    return result.toString()
  }
  
  private function executeQuery() : List<T> {
    var profiler = Util.newProfiler(_profilerTag)
    var sql = computeSql()
    profiler.start(sql + " (" + Arrays.asList(_parameters) + ")")
    try {
      return _db.getDBExecutionKernel().executeSelect(sql,
          _resultProcessor,
          _parameters)
    } finally {
      profiler.stop()
    }
  }
  
  private class OrderByInfo {
    var _sortColumn : IPropertyReference<IDBObject, Object>
    var _direction : QueryResult.SortDirection
    var _sql : String

    construct(sortColumn : IPropertyReference<IDBObject, Object>, direction : QueryResult.SortDirection) {
      _sortColumn = sortColumn
      _direction = direction
    }

    construct(sql : String) {
      _sql = sql
    }

    function asSql() : String {
      if (_sql != null) {
        return _sql
      } else {
        // TODO - AHK - Quoting
        return "\"" + _sortColumn.PropertyInfo["ColumnName"] + "\"" + (_direction == ASC ? " ASC" : " DESC")
      }
    }
  }
}
