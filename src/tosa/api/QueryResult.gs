package tosa.api

uses java.lang.Iterable

/**
 * This class represents the results of a particular query.
 *
 * @author akeefer
 */
public interface QueryResult<T> extends Iterable<T> {

  // TODO - AHK - Should these be longs?

  function size() : int

  property get Count() : int

  // TODO - AHK - Yeahhh
  function get(idx : int) : T
}
