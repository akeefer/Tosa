package tosa.api

uses java.lang.Iterable
uses gw.lang.reflect.features.IPropertyReference

/**
 * This class represents the results of a particular query.  The QueryResult class
 * is essentially a lazily-loaded Iterable containing the results of the query.  The query itself
 * isn't actually issued until a method like get() or iterator() is called, at which point
 * the query, modified by any ordering or paging, is then executed.
 *
 * @author akeefer
 */
public interface QueryResult<T> extends Iterable<T> {

  /**
   * Enum used to indicate the direction of a sort.
   */
  public enum SortDirection { ASC, DESC }

  /**
   * Returns the number of entries in this result set.  This method will currently throw if
   * called when paging has been applied to the query.  Calling this will cause the results
   * to be loaded.  This method is identical to the Count property.
   *
   * @return the number of entries in the result set
   */
  function size() : int

  /**
   * Returns the number of entries in this result set.  This property will currently throw if
   * called when paging has been applied to the query.  Calling this will cause the results
   * to be loaded.  This property is identical to the size() method.
   *
   * @return the number of entries in the result set
   */
  property get Count() : int

  /**
   * Returns the result at the given index.  For paged queries, this index is relative to the initial
   * offset of the query, i.e. if the startOffset is 100, get(15) will return result #115 in the result
   * set.
   *
   * @param idx the relative index of the result
   * @return the result at the given position in the result set
   */
  function get(idx : int) : T

  /**
   * Adds an ORDER BY predicate to this QueryResult, sorting on the specified property and direction.  If
   * no direction is specified, the default will be ASC.  Note that the property reference should be a
   * database property that's on the root entity type for this result set.  Multiple orderBy predicates
   * can be added (and combined with orderBySql predicates).
   *
   * Calling this method will invalidate any already-loaded results, causing them to be reloaded from the
   * database on the next call that requires the results to be loaded.  This method will return this same
   * QueryResult back for the sake of method chaining.
   *
   * @param sortColumn the column to sort on
   * @param sortDirection the direction to sort, defaulting to ASC
   * @return this same object
   */
  function orderBy(sortColumn : IPropertyReference<IDBObject, Object>, sortDirection : SortDirection = ASC) : QueryResult<T>

  /**
   * Adds an ORDER BY predicate to this QueryResult, sorting using the specified SQL.  The
   * SQL passed as an argument to this method should *not* contain the words ORDER BY,
   * as those will automatically be added in.  Multiple orderBySql predicates can be added
   * and combined with orderBy predicates.
   *
   * Calling this method will invalidate any already-loaded results, causing them to be reloaded from the
   * database on the next call that requires the results to be loaded.  This method will return this same
   * QueryResult back for the sake of method chaining.
   *
   * @param sql the ORDER BY predicate to apply
   * @return this same object
   */
  function orderBySql(sql : String) : QueryResult<T>

  /**
   * Clears an predicates applied with orderBy or orderBySql.
   *
   * Calling this method will invalidate any already-loaded results, causing them to be reloaded from the
   * database on the next call that requires the results to be loaded.  This method will return this same
   * QueryResult back for the sake of method chaining.
   *
   * @return this same object
   */
  function clearOrderBys() : QueryResult<T>

  /**
   * Applies paging to this query, which means that A) the results will be loaded from the
   * database starting from the specified offset and B) that results will be loaded in pageSize
   * chunks at a time.  Calling get() on a paged query will evaluate the index relative to the
   * start offset for the query.  Calling iterator() will return an iterator that begins at
   * the specified start index, and which iterates through the result of the result in chunks
   * of pageSize results.  Only one out of startPage or startOffset should be specified, and
   * startPage and startOff are considered to be 0-indexed (i.e. startPage or startOffset of 0 starts
   * from the beginning of the result set).
   *
   * Calling this method will invalidate any already-loaded results, causing them to be reloaded from the
   * database on the next call that requires the results to be loaded.  This method will return this same
   * QueryResult back for the sake of method chaining.
   *
   * @param startPage if specified, the initial offset will be startPage * pageSize
   * @param pageSize the number of results to load at a time
   * @param startOffset if specified, the initial offset to being loading results from
   *
   * @return this same object
   */
  function page(startPage : int = 0, pageSize : int = 100, startOffset : int = 0) : QueryResult<T>

  /**
   * Loads the specified page into a List.  If this query hasn't been paged, this will
   * load all results regardless of the pageNumber argument specified.
   *
   * @param pageNumber the page number to load, which defaults to 0
   *
   * @return the results corresponding to the specified page
   */
  function loadPage(pageNumber : int = 0) : List<T>

  /**
   * Clears any paging information applied to this QueryResult.
   *
   * Calling this method will invalidate any already-loaded results, causing them to be reloaded from the
   * database on the next call that requires the results to be loaded.  This method will return this same
   * QueryResult back for the sake of method chaining.
   *
   * @return this same object
   */
  function clearPaging() : QueryResult<T>

  /**
   * Clears any paging information as well as any order by predicates applied to this QueryResult.
   *
   * Calling this method will invalidate any already-loaded results, causing them to be reloaded from the
   * database on the next call that requires the results to be loaded.  This method will return this same
   * QueryResult back for the sake of method chaining.
   *
   * @return this same object
   */
  function clear() : QueryResult<T>

  /**
   * Returns a clone of this QueryResult object.  The clone will initially have the same set of
   * order by predicates and the same startPage/pageSize/startOffset, but will not retain any information
   * about the currently-loaded results or the current place within that result set.  If a copy of
   * the original, unmodified query is desired instead, this method can be chained with the clear() method,
   * i.e. queryResult.clone().clear().
   *
   * @return a clone of this QueryResult that contains the same initial query, order by predicates,
   *         and paging information
   */
  function clone() : QueryResult<T>

}
