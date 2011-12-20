package tosa.api;

/**
 * This class represents the results of a particular query.
 *
 * @author akeefer
 */
public interface QueryResult<T> extends Iterable<T> {

  // TODO - AHK - Should these be longs?

  int size();

  int getCount();

  // TODO - AHK - Yeahhh
  T get(int index);
}
