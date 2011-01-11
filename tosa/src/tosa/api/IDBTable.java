package tosa.api;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/9/11
 * Time: 4:30 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBTable {

  // TODO - AHK - Other attributes
  // TODO - AHK - Should this have a getColumn() method?

  IDatabase getDatabase();

  String getName();

  IDBColumn getColumn(String name);

  // TODO - AHK - Should this be an iterable?
  Collection<? extends IDBColumn> getColumns();

  Collection<? extends IDBColumn> getIncomingFKs();

  boolean hasId();

  // TODO - AHK - Some attribute to indicate it's a join table?
}
