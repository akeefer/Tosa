package tosa.api.query;

import gw.lang.reflect.features.PropertyReference;
import tosa.api.IDBObject;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/28/11
 * Time: 8:23 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CoreFinder<T extends IDBObject> {
  // TODO - AHK - Javadoc and packaging

  // TODO - AHK - Return some sort of query result object, instead of List?

  T fromId(long id);

  // TODO - AHK - Make this return a long
  int countWithSql(String sql);

  // TODO - AHK - Make this return a long
  int count(T template);

  List<T> findWithSql(String sql);

  List<T> find(T template);

  List<T> findSorted(T template, PropertyReference sortColumn, boolean ascending);

  List<T> findPaged(T template, int pageSize, int offset);

  List<T> findSortedPaged(T template, PropertyReference sortColumn, boolean ascending,  int pageSize, int offset);
}
