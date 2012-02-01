package tosa.api

uses java.lang.Iterable
uses gw.lang.reflect.features.IPropertyReference

/**
 * This class represents the results of a particular query.
 *
 * @author akeefer
 */
public interface QueryResult<T> extends Iterable<T> {

  // TODO - AHK - Should these be longs?
  public enum SortDirection { ASC, DESC }

  function size() : int

  property get Count() : int

  // TODO - AHK - Yeahhh
  function get(idx : int) : T

  function orderBy(sortColumn : IPropertyReference<IDBObject, Object>, sortDirection : SortDirection = ASC) : QueryResult<T>

  function orderBySql(sql : String) : QueryResult<T>


}
