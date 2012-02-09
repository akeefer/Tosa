package tosa.impl

uses tosa.TosaDBTestBase
uses org.junit.Test
uses tosa.api.CoreFinder
uses tosa.api.IDBObject
uses tosa.CachedDBObject
uses gw.lang.reflect.TypeSystem
uses tosa.loader.IDBType
uses gw.lang.reflect.ReflectUtil
uses tosa.api.QueryResult
uses tosa.dbmd.PreparedStatementParameterImpl
uses java.sql.Types
uses tosa.api.IPreparedStatementParameter
uses java.lang.Exception
uses gw.lang.reflect.features.IPropertyReference
uses test.testdb.Foo
uses java.lang.Throwable
uses org.junit.BeforeClass

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/8/12
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
class QueryResultImplTest extends TosaDBTestBase {

  @BeforeClass
  static function createSampleData() {
    newFoo(:firstName = "Alice", "Smith")
    newFoo(:firstName = "Bob", "Walker")
    newFoo(:firstName = "Chris", "Smith")
    newFoo(:firstName = "Dan", "Jones")
    newFoo(:firstName = "Emily", "Jones")
  }

  override function beforeTestMethod() {
    // do nothing
  }

  private static function newFoo(firstName: String = null, lastName: String = null): IDBObject {
    var foo = ReflectUtil.construct("test.testdb.Foo", {}) as IDBObject
    foo["FirstName"] = firstName
    foo["LastName"] = lastName
    foo.update()
    return foo
  }
  
  private property get FooType() : IDBType {
    return TypeSystem.getByFullName("test.testdb.Foo") as IDBType
  }

  // TODO - AHK - How do I create these without referencing Foo?
  private property get FooFirstName() : IPropertyReference<IDBObject, Object> {
    return Foo#FirstName
  }
  
  private property get FooLastName() : IPropertyReference<IDBObject, Object> {
    return Foo#LastName
  }

  private property get OrderedByFirstNameAsc() : List<String> {
    return {"Alice", "Bob", "Chris", "Dan", "Emily"}
  }

  private property get OrderedByFirstNameDesc() : List<String> {
    return {"Emily", "Dan", "Chris", "Bob", "Alice"}
  }

  private property get OrderedByLastNameAscThenFirstNameAsc() : List<String> {
    return {"Dan", "Emily", "Alice", "Chris", "Bob"}
  }
  
  private function fooQuery(firstName : String = null) : QueryResult {
    var sql = (firstName == null ? "SELECT * FROM Foo" : "SELECT * FROM Foo WHERE FirstName = ?")
    var params : IPreparedStatementParameter[] = (firstName == null ? {} : {new PreparedStatementParameterImpl(firstName, Types.VARCHAR)})
    
    return new QueryResultImpl(
        "testTag",
            sql,
            params,
            getDB(),
            \r -> CoreFinder.buildObject(FooType, r))  
  }
  
  private function assertException(callback : block()) {
    try {
      callback()
      fail("Expected an exception to be thrown")
    } catch (t : Exception) {
      // Expected
    }  
  }

  // ===================== Count and size() =============================

  // TODO - AHK - Is this useful?
  @Test
  function testCountAndSizeReturnZeroForEmptyResultsSet() {
    var queryResult = fooQuery("NoSuchName")
    assertEquals(0, queryResult.Count)
    assertEquals(0, queryResult.size())
  }
  
  @Test
  function testCountReturnsCorrectValueForNonEmptyResultSet() {
    var queryResult = fooQuery()
    assertEquals(5, queryResult.Count)
    assertEquals(5, queryResult.size())
  }

  @Test
  function testCountReturnsNonZeroValueForNonEmptyResultsSetWithParameters() {
    var queryResult = fooQuery(:firstName = "Bob")
    assertEquals(1, queryResult.Count)
    assertEquals(1, queryResult.size())
  }
  
  @Test
  function testCountThrowsIfPagingHasBeenSet() {
    var queryResult = fooQuery().page()
    assertException(\ -> queryResult.Count )
  }

  // ========================== orderBy =================================================

  @Test
  function testOrderByThrowsIfSortColumnIsNotADBPropertyOnTheCorrectTable() {
    // TODO - AHK
  }

  @Test
  function testOrderByInvalidatesResultsIfQueryResultsHaveAlreadyBeenLoaded() {
    var queryResult = fooQuery()
    queryResult.get(0)
    queryResult.orderBy(FooFirstName, DESC)
    assertEquals("Emily", queryResult.get(0)["FirstName"])
  }

  @Test
  function testOrderByWithSimpleArgument() {
    var queryResult = fooQuery().orderBy(FooFirstName)
    assertEquals(OrderedByFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  @Test
  function testOrderByRespectsDirectionArgument() {
    var queryResult = fooQuery().orderBy(FooFirstName, DESC)
    assertEquals(OrderedByFirstNameDesc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  @Test
  function testMultipleOrderBys() {
    var queryResult = fooQuery().orderBy(FooLastName).orderBy(FooFirstName)
    assertEquals(OrderedByLastNameAscThenFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  // ================================ orderBySql =====================================

  @Test
  function testOrderBySqlThrowsIfSqlStartsWithOrderBy() {
    var queryResult = fooQuery()
    assertException(\ -> queryResult.orderBySql("ORDER BY FirstName ASC"))
  }

  @Test
  function testOrderBySqlThrowsIfSqlStartsWithOrderByInMixedCase() {
    var queryResult = fooQuery()
    assertException(\ -> queryResult.orderBySql("oRder bY FirstName ASC"))
  }

  @Test
  function testOrderBySqlAsOnlyOrderByPredicate() {
    var queryResult = fooQuery().orderBySql("FirstName ASC")
    assertEquals(OrderedByFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  @Test
  function testMultipleOrderBySqlPredicates() {
    var queryResult = fooQuery().orderBySql("LastName ASC").orderBySql("FirstName ASC")
    assertEquals(OrderedByLastNameAscThenFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  @Test
  function testOrderBySqlInvalidatesResultsThrowsIfQueryResultsHaveAlreadyBeenLoaded() {
    var queryResult = fooQuery()
    queryResult.get(0)
    queryResult.orderBySql("FirstName DESC")
    assertEquals("Emily", queryResult.get(0)["FirstName"])
  }

  @Test
  function testOrderBySqlFollowedByOrderBy() {
    var queryResult = fooQuery().orderBySql("LastName ASC").orderBy(FooFirstName)
    assertEquals(OrderedByLastNameAscThenFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  @Test
  function testOrderByFollowedByOrderBySql() {
    var queryResult = fooQuery().orderBy(FooLastName).orderBySql("FirstName ASC")
    assertEquals(OrderedByLastNameAscThenFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  // =================================== clearOrderBys ================================

  @Test
  function testClearOrderBysClearsRegularOrderBy() {
    var queryResult = fooQuery().orderBy(FooLastName)
    queryResult.clearOrderBys()
    assertEquals(OrderedByFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  @Test
  function testClearOrderBysInvalidatesResults() {
    var queryResult = fooQuery().orderBy(FooLastName)
    queryResult.get(0)
    queryResult.clearOrderBys()
    assertEquals(OrderedByFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  @Test
  function testClearOrderBysClearsOrderBySql() {
    var queryResult = fooQuery().orderBySql("LastName ASC")
    queryResult.clearOrderBys()
    assertEquals(OrderedByFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  @Test
  function testClearOrderBysWithMultipleOrderByClauses() {
    var queryResult = fooQuery().orderBySql("LastName ASC").orderBy(FooFirstName)
    queryResult.clearOrderBys()
    assertEquals(OrderedByFirstNameAsc, queryResult.toList().map( \ r -> r["FirstName"]))
  }

  // ===================================== page() ===================================

  @Test
  function testPageInvalidatesResultsIfQueryHasAlreadyBeenExecuted() {
    var queryResult = fooQuery().orderBy(FooFirstName)
    queryResult.get(0)
    queryResult.page(:startOffset = 4)
    assertEquals("Emily", queryResult.get(0)["FirstName"])
  }

  @Test
  function testPageThrowsIfStartPageAndStartOffsetAreBothGreaterThanZero() {
    var queryResult = fooQuery().orderBy(FooFirstName)
    assertException(\ -> queryResult.page(:startPage = 2, :startOffset = 1))
  }

  @Test
  function testPageThrowsIfStartPageAndStartOffsetAreBothLessThanZero() {
    var queryResult = fooQuery().orderBy(FooFirstName)
    assertException(\ -> queryResult.page(:startPage = -1, :startOffset = -1))
  }

  @Test
  function testPageThrowsIfPageSizeIsZero() {
    var queryResult = fooQuery().orderBy(FooFirstName)
    assertException(\ -> queryResult.page(:pageSize = 0))
  }

  @Test
  function testPageThrowsIfPageSizeIsLessThanZero() {
    var queryResult = fooQuery().orderBy(FooFirstName)
    assertException(\ -> queryResult.page(:pageSize = -5))
  }

  @Test
  function testGetOnPagedQueryForValueOnCurrentPage() {
    var queryResult = fooQuery().orderBy(FooFirstName).page(:startPage = 1, :pageSize = 2)
    assertEquals("Chris", queryResult.get(0)["FirstName"])
  }

  @Test
  function testGetOnPagedQueryForValueOnOtherPage() {
    var queryResult = fooQuery().orderBy(FooFirstName).page(:startPage = 1, :pageSize = 2)
    assertEquals("Emily", queryResult.get(2)["FirstName"])
  }

  @Test
  function testGetOnPagedQueryIsIndexedRelativeToStartOffset() {
    var queryResult = fooQuery().orderBy(FooFirstName).page(:pageSize = 2, :startOffset = 2)
    assertEquals("Emily", queryResult.get(2)["FirstName"])
  }

  @Test
  function testUsingGetToLoadEachResultInTurn() {
    var queryResult = fooQuery().orderBy(FooFirstName).page(:pageSize = 2)
    assertEquals("Alice", queryResult.get(0)["FirstName"])
    assertEquals("Bob", queryResult.get(1)["FirstName"])
    assertEquals("Chris", queryResult.get(2)["FirstName"])
    assertEquals("Dan", queryResult.get(3)["FirstName"])
    assertEquals("Emily", queryResult.get(4)["FirstName"])
  }

  // TODO - AHK - It's unclear to me what the right thing is here
//  @Test
//  function testExecutingPagedQueryThrowsIfNoOrderByIsSpecified() {
//    var queryResult = fooQuery().page(:pageSize = 2)
//    assertException(\ -> queryResult.get(0))
//  }

  @Test
  function testIteratorForPagedQueryStartsAtStartPage() {
    var queryResult = fooQuery().orderBy(FooFirstName).page(:startPage = 1, :pageSize = 2)
    assertEquals("Chris", queryResult.iterator().next()["FirstName"])
  }

  @Test
  function testIteratorForPagedQueryStartsAtStartOffset() {
    var queryResult = fooQuery().orderBy(FooFirstName).page(:pageSize = 2, :startOffset = 3)
    assertEquals("Dan", queryResult.iterator().next()["FirstName"])
  }

  @Test
  function testIteratorForPagedQueryIteratesThroughAllPages() {
    var it = fooQuery().orderBy(FooFirstName).page(:pageSize = 2).iterator()
    assertEquals("Alice", it.next()["FirstName"])
    assertTrue(it.hasNext())
    assertEquals("Bob", it.next()["FirstName"])
    assertTrue(it.hasNext())
    assertEquals("Chris", it.next()["FirstName"])
    assertTrue(it.hasNext())
    assertEquals("Dan", it.next()["FirstName"])
    assertTrue(it.hasNext())
    assertEquals("Emily", it.next()["FirstName"])
    assertFalse(it.hasNext())
    assertException(\ -> it.next())
  }

  // =============================== clearPaging() =================

  @Test
  function testClearPagingClearsPagingInformation() {
    var queryResult = fooQuery().orderBy(FooFirstName).page(:pageSize = 2, :startOffset = 3)
    queryResult.clearPaging()
    assertEquals("Alice", queryResult.get(0)["FirstName"])
  }

  // =============================== clear() =================

  @Test
  function testClearClearsBothPagingAndOrdering() {
    var queryResult = fooQuery().orderBy(FooLastName).page(:pageSize = 2, :startOffset = 3)
    queryResult.clear().orderBy(FooFirstName)
    assertEquals("Alice", queryResult.get(0)["FirstName"])
  }

  // ================================== clone() ================

  @Test
  function testCloneCreatesCloneOfQueryAlongWithOrderByAndPagingInformation() {
    var queryResult = fooQuery().orderBy(FooFirstName, DESC).page(:pageSize = 2, :startOffset = 3)
    var newQuery = queryResult.clone()
    assertFalse(queryResult === newQuery)
    assertEquals("Bob", queryResult.get(0)["FirstName"])
    assertEquals("Bob", newQuery.get(0)["FirstName"])
    newQuery.clear().orderBy(FooFirstName)
    assertEquals("Bob", queryResult.get(0)["FirstName"])
    assertEquals("Alice", newQuery.get(0)["FirstName"])
  }
  /*

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
        var column = _sortColumn.PropertyInfo["Column"] as IDBColumn
        return column.PossiblyQuotedName + (_direction == ASC ? " ASC" : " DESC")
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
  }*/
}