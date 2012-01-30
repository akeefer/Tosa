package tosa.loader

uses tosa.loader.IDBType
uses tosa.api.IDBObject
uses gw.lang.reflect.features.PropertyReference
uses tosa.impl.util.StringSubstituter
uses tosa.impl.query.SqlStringSubstituter
uses java.util.Map
uses gw.util.Pair
uses tosa.api.QueryResult
uses java.lang.IllegalArgumentException
uses tosa.api.CoreFinder

/**
 * This class effectively serves as a static mix-in for the DBType types.  Each function in here
 * needs to have its first argument be an IDBType object.  In addition, parameters that are
 * typed as IDBObject will be transformed at delegation time to the type in question, i.e.
 * something like tosa.testdb.Foo will be used in place of IDBObject, providing a sort of
 * covariance on the delegated type.  Any getter/setter pairs here will be turned into
 * properties for delegation purposes.  Every function on this class needs to be static.
 *
 * These methods SHOULD NOT be called directly by code:  they're purely here for delegation
 * purposes, to make it easier (and clearer) to write the static methods that are automatically
 * added to every DBType.
 */
class DBTypeDelegate {

  /**
   * Loads the entity with the given id.  Returns null if no entity exists with that id.
   *
   * @param dbType the implicit dbType initial argument
   * @param id the id to load
   */
  static function fromId(dbType : IDBType, id : long) : IDBObject {
    return new CoreFinder(dbType).fromId(id)
  }

  static function count(dbType : IDBType, sql : String, params : Map<String, Object> = null) : long {
    return new CoreFinder(dbType).count(sql, params)
  }

  static function countAll(dbType : IDBType) : long {
    return new CoreFinder(dbType).countAll()
  }

  static function countWhere(dbType : IDBType, sql : String, params : Map<String, Object> = null) : long {
    return new CoreFinder(dbType).countWhere(sql, params)
  }

  static function countLike(dbType : IDBType, template: IDBObject) : long {
    return new CoreFinder(dbType).countLike(template)
  }

  static function select(dbType : IDBType, sql : String, params : Map<String, Object> = null) : QueryResult<IDBObject> {
    return new CoreFinder(dbType).select(sql, params)
  }

  static function selectAll(dbType : IDBType) : QueryResult<IDBObject> {
    return new CoreFinder(dbType).selectAll()
  }

  static function selectWhere(dbType : IDBType, sql : String, params : Map<String, Object> = null) : QueryResult<IDBObject> {
    return new CoreFinder(dbType).selectWhere(sql, params)
  }

  static function selectLike(dbType : IDBType, template : IDBObject) : QueryResult<IDBObject> {
    return new CoreFinder(dbType).selectLike(template)
  }

  static function findSorted(dbType : IDBType, template : IDBObject, sortProperty : PropertyReference<IDBObject, Object>, ascending : boolean) : List<IDBObject> {
    return new CoreFinder(dbType).findSorted(template, sortProperty, ascending)
  }

  static function findPaged(dbType : IDBType, template : IDBObject, pageSize : int, offset : int) : List<IDBObject> {
    return new CoreFinder(dbType).findPaged(template, pageSize, offset)
  }

  static function findSortedPaged(dbType : IDBType, template : IDBObject, sortProperty : PropertyReference<IDBObject, Object>, ascending : boolean, pageSize : int, offset : int) : List<IDBObject> {
    return new CoreFinder(dbType).findSortedPaged(template, sortProperty, ascending, pageSize, offset)
  }
}