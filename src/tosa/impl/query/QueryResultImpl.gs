package tosa.impl.query

uses tosa.api.QueryResult
uses tosa.api.IPreparedStatementParameter
uses tosa.api.IQueryResultProcessor
uses tosa.api.IDatabase
uses java.util.Iterator
uses tosa.loader.Util
uses java.util.Arrays

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

  public construct(profilerTag : String, originalQuery : String, parameters : IPreparedStatementParameter[], db : IDatabase, resultProcessor : IQueryResultProcessor<T>) {
    _profilerTag = profilerTag
    _originalQuery = originalQuery
    _parameters = parameters
    _db = db
    _resultProcessor = resultProcessor
  }

  override property get Count() : int {
    maybeLoadResults()
    return _results.size()
  }

  override function size() : int {
    return Count
  }

  override function iterator() : Iterator<T> {
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

  private function executeQuery() : List<T> {
    var profiler = Util.newProfiler(_profilerTag)
    profiler.start(_originalQuery + " (" + Arrays.asList(_parameters) + ")")
    try {
      return _db.getDBExecutionKernel().executeSelect(_originalQuery,
          _resultProcessor,
          _parameters)
    } finally {
      profiler.stop()
    }
  }
}
