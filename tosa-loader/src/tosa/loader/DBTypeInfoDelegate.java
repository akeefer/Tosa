package tosa.loader;

import tosa.api.IDBObject;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/9/12
 * Time: 12:58 AM
 * To change this template use File | Settings | File Templates.
 */
public interface DBTypeInfoDelegate {
  
  IDBObject fromId(IDBType dbType, long id);
  
  long count(IDBType dbType, String sql, Map<String, Object> params);
  
  long countAll(IDBType dbType);
  
  long countWhere(IDBType dbType, String sql, Map<String, Object> params);
  
  long countLike(IDBType dbType, IDBObject template);
  
  Object select(IDBType dbType, String sql, Map<String, Object> params);
  
  Object selectAll(IDBType dbType);
  
  Object selectWhere(IDBType dbType, String sql, Map<String, Object> params);
  
  Object selectLike(IDBType dbType, IDBObject template);
}
