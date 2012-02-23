package tosa.impl

uses tosa.loader.IDBType
uses java.util.Map

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/9/12
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
class CachedDBObjectDelegateFactoryImpl implements CachedDBObject.DelegateFactory {
  override function createDelegate(type : IDBType, originalValues : Map<String, Object>, owner : CachedDBObject) : CachedDBObject.Delegate {
    return new CachedDBObjectDelegateImpl(type, originalValues, owner)
  }
}