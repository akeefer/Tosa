package tosa.impl

uses tosa.loader.IDBType
/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/9/12
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
class CachedDBObjectDelegateFactoryImpl implements CachedDBObject.DelegateFactory {
  override function createDelegate(type : IDBType, isNew : boolean, owner : CachedDBObject) : CachedDBObject.Delegate {
    return new CachedDBObjectDelegateImpl(type, isNew, owner)
  }
}