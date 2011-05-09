package tosa.api;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/8/11
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
// TODO - AHK - This class probably needs some help with the name
public interface EntityCollection<T extends IDBObject> extends Iterable<T> {

  int size();

  T get(int index);

  void add(T element);

  void remove(T element);
}
