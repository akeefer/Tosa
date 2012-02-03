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
uses java.lang.UnsupportedOperationException
uses java.lang.System
uses tosa.dbmd.PreparedStatementParameterImpl
uses java.sql.Types
uses java.util.NoSuchElementException

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

  var _pagingInfo : PagingInfo

  public construct(profilerTag : String, originalQuery : String, parameters : IPreparedStatementParameter[], db : IDatabase, resultProcessor : IQueryResultProcessor<T>) {
    _profilerTag = profilerTag
    _originalQuery = originalQuery
    _parameters = parameters
    _db = db
    _resultProcessor = resultProcessor
    _orderBys = new ArrayList<OrderByInfo>()
  }

  override property get Count() : int {
    // TODO - AHK - This has to work totally differently if it's paged
    if (_pagingInfo != null) {
      throw new UnsupportedOperationException("Tosa can't currently do a count on a paged query")
    } else {
      maybeLoadResults()
      return _results.size()
    }
  }

  override function size() : int {
    return Count
  }

  override function orderBySql(sql : String): QueryResult<T> {
    // TODO - AHK - Throw if the query has already been evaluated
    _orderBys.add(new OrderByInfo(sql))
    return this
  }

  override function orderBy(sortColumn : IPropertyReference<IDBObject, Object>, sortDirection: QueryResult.SortDirection): QueryResult<T> {
    // TODO - AHK - Throw if the query has already been evaluated
    _orderBys.add(new OrderByInfo(sortColumn, sortDirection))
    return this
  }

  // TODO - AHK - Some way to jump to a particular page?
  
  override function page(startPage : int, pageSize : int, startOffset : int) : QueryResult<T> {
    // TODO - AHK - Throw if the query has already been evaluated?  Or just invalidate iterators?  Or what?
    if (startOffset > 0 and startPage > 0) {
      throw "the call to page can specify either startPage or startOffset, but not both"
    }
    // TODO - AHK - Lots more argument validation
    var realStartOffset = (startOffset > 0 ? startOffset : pageSize * startOffset)
    _pagingInfo = new PagingInfo(startPage, pageSize)
    _results = null
    return this
  }

  override function iterator(): Iterator <T> {
    if (_pagingInfo != null) {
      // Set it to the first page of results so we always start with a "clean" iterator starting from the
      // first element returned by the query
      resetToInitialOffset()
      maybeLoadResults()
      return new PagingIterator(_results.iterator())
    } else {
      maybeLoadResults()
      return _results.iterator()
    }
  }

  override function get(idx : int) : T {
    if (_pagingInfo != null) {
      if (_pagingInfo.containsIndex(idx)) {
        maybeLoadResults()
        return _results.get(idx - _pagingInfo._currentOffset)
      } else {
        // For now, we just keep one page of results.  If they ask for a result
        // on a different page, we load that page and throw out the old one
        _pagingInfo.setToPageForIndex(idx)
        _results = null
        return get(idx)
      }
    } else {
      maybeLoadResults()
      return _results.get(idx)
    }
  }
  
  private function maybeLoadResults() {
    if (_results == null) {
      _results = executeQuery()
    }
  }

  private function computeSql() : SqlAndParameters {
    var query = _originalQuery
    var parameters = _parameters
    
    if (not _orderBys.Empty) {
      query = query + buildOrderByClause()
    }

    if (_pagingInfo != null) {
      // TODO - AHK - Do we need to impose an order here?
      query = query + " LIMIT ? OFFSET ?"
      var newParameters = new ArrayList<IPreparedStatementParameter>(Arrays.asList(parameters))
      newParameters.add(new PreparedStatementParameterImpl(_pagingInfo.PageSize, Types.BIGINT))
      newParameters.add(new PreparedStatementParameterImpl(_pagingInfo.CurrentOffset, Types.BIGINT))
      parameters = newParameters.toTypedArray()
    }

    return new SqlAndParameters(query, parameters)
  }
  
  private function buildOrderByClause() : String {
    var result = new StringBuilder()
    result.append(" ORDER BY ")
    result.append(_orderBys.map( \ o -> o.asSql()).join(", "))
    return result.toString()
  }
  
  private function executeQuery() : List<T> {
    var profiler = Util.newProfiler(_profilerTag)
    var sqlAndParameters = computeSql()
    profiler.start(sqlAndParameters.Sql + " (" + Arrays.asList(sqlAndParameters.Parameters) + ")")
    try {
      return _db.getDBExecutionKernel().executeSelect(sqlAndParameters.Sql,
          _resultProcessor,
          sqlAndParameters.Parameters)
    } finally {
      profiler.stop()
    }
  }
  
  private function incrementPage() {
    _pagingInfo.incrementPage()
    _results = null
  }

  private function resetToInitialOffset() {
    if (_pagingInfo._currentOffset != _pagingInfo._startOffset) {
      _results = null
      _pagingInfo.resetToInitialOffset()
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

  private class PagingInfo {
    var _startOffset : int as readonly StartOffset
    var _currentOffset : int as readonly CurrentOffset
    var _pageSize : int as readonly PageSize

    construct(startOffsetArg : int, pageSizeArg : int) {
      _startOffset = startOffsetArg
      _pageSize = pageSizeArg
    }
    
    function containsIndex(idx : int) : boolean {
      var relativeStartIndex = _currentOffset - _startOffset
      return (idx >= relativeStartIndex and idx < relativeStartIndex + _pageSize)
    }
    
    function setToPageForIndex(idx : int) {
      var appropriatePage = idx / _pageSize
      var pageStart = _pageSize * appropriatePage
      _currentOffset = pageStart + _startOffset 
    }
    
    function incrementPage() {
      _currentOffset = _currentOffset + _pageSize
    }

    function resetToInitialOffset() {
      _currentOffset = _startOffset
    }
  }

  private class SqlAndParameters {
    var _sql : String as Sql
    var _parameters : IPreparedStatementParameter[] as Parameters

    construct(sqlArg : String, parametersArg : IPreparedStatementParameter[]) {
      _sql = sqlArg
      _parameters = parametersArg
    }
  }
  
  private class PagingIterator implements Iterator<T> {
    
    var _delegate : Iterator<T>

    construct(delegateArg : Iterator<T>) {
      _delegate = delegateArg
    }
    
    override function hasNext() : boolean {
      if (_delegate.hasNext()) {
        return true
      } else {
        incrementPage()
        maybeLoadResults()
        _delegate = _results.iterator()
        return _delegate.hasNext()
      }
    }

    override function next() : T {
      if (!hasNext()) {
        throw new NoSuchElementException()
      }
      return _delegate.next()
    }

    override function remove() {
      throw new UnsupportedOperationException()
    }
  }
}
