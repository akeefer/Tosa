package tosa.impl.loader

uses tosa.loader.DBTypeInfoDelegate
uses tosa.loader.IDBType
uses tosa.api.IDBObject
uses java.util.Map
uses tosa.api.CoreFinder
uses tosa.CachedDBObject

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/9/12
 * Time: 1:18 AM
 * To change this template use File | Settings | File Templates.
 */
class DBTypeInfoDelegateImpl implements DBTypeInfoDelegate {

  override function fromId(dbType : IDBType, id : long) : IDBObject {
    return new CoreFinder(dbType).fromId(id)
  }

  override function count(dbType : IDBType, sql : String, params : Map<String, Object>) : long {
    return new CoreFinder(dbType).count(sql, params)
  }

  override function countAll(dbType : IDBType) : long {
    return new CoreFinder(dbType).countAll()
  }

  override function countWhere(dbType : IDBType, sql : String, params : Map<String, Object>) : long {
    return new CoreFinder(dbType).countWhere(sql, params)
  }

  override function countLike(dbType : IDBType, template : IDBObject) : long {
    return new CoreFinder(dbType).countLike(template)
  }

  override function select(dbType : IDBType, sql : String, params : Map<String, Object>) : java.lang.Object {
    return new CoreFinder(dbType).select(sql, params)
  }

  override function selectAll(dbType : IDBType) : java.lang.Object {
    return new CoreFinder(dbType).selectAll()
  }

  override function selectWhere(dbType : IDBType, sql : String, params : Map<String, Object>) : java.lang.Object {
    return new CoreFinder(dbType).selectWhere(sql, params)
  }

  override function selectLike(dbType : IDBType, template : IDBObject) : java.lang.Object {
    return new CoreFinder(dbType).selectLike(template)
  }

  override function newInstance(dbType : IDBType, isNew : boolean) : IDBObject {
    return new CachedDBObject(dbType, isNew)
  }
}