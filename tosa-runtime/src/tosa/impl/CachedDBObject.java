package tosa.impl;

import gw.lang.reflect.IType;
import gw.lang.reflect.ReflectUtil;
import gw.util.GosuRefactorUtil;
import gw.util.concurrent.LocklessLazyVar;
import tosa.api.EntityCollection;
import tosa.api.IDBArray;
import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.loader.DBTypeInfo;
import tosa.loader.IDBType;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/9/12
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CachedDBObject implements IDBObject {

  // We've got a factory that we can just call through to, and a lazy static instance of that factory,
  // so that we don't have to reflectively-construct the delegate every time we create one of these objects
  public static interface DelegateFactory {
    Delegate createDelegate(IDBType type, Map<String, Object> originalValues, CachedDBObject owner);
  }

  private static LocklessLazyVar<DelegateFactory> DELEGATE_FACTORY = new LocklessLazyVar<DelegateFactory>() {
    @Override
    protected DelegateFactory init() {
      return ReflectUtil.construct("tosa.impl.CachedDBObjectDelegateFactoryImpl");
    }
  };

  private IDBType _type;
  private Delegate _delegate;
  
  public CachedDBObject(IDBType type, Map<String, Object> originalValues) {
    _type = type;
    _delegate = DELEGATE_FACTORY.get().createDelegate(type, originalValues, this);
  }
  
  @Override
  public IType getIntrinsicType() {
    return _type;
  }

  @Override
  public IDBTable getDBTable() {
    return _delegate.getDBTable();
  }

  @Override
  public Object getColumnValue(String s) {
    return _delegate.getColumnValue(s);
  }

  @Override
  public void setColumnValue(String s, Object o) {
    _delegate.setColumnValue(s, o);
  }

  @Override
  public IDBObject getFkValue(String s) {
    return _delegate.getFkValue(s);
  }

  @Override
  public void setFkValue(String s, IDBObject idbObject) {
    _delegate.setFkValue(s, idbObject);
  }

  @Override
  public EntityCollection getArray(String s) {
    return _delegate.getArray(s);
  }

  @Override
  public EntityCollection getArray(IDBArray idbArray) {
    return _delegate.getArray(idbArray);
  }

  @Override
  public Long getId() {
    return _delegate.getId();
  }

  @Override
  public Long toID() {
    return _delegate.toID();
  }

  @Override
  public boolean isNew() {
    return _delegate.isNew();
  }

  @Override
  public void update() {
    _delegate.update();
  }

  @Override
  public void delete() {
    _delegate.delete();
  }

  @Override
  public int hashCode() {
    return _delegate.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (obj instanceof CachedDBObject) {
      return _delegate.equals(((CachedDBObject) obj)._delegate);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return _delegate.toString();
  }

  public interface Delegate {
    tosa.api.IDBTable getDBTable();

    java.lang.Object getColumnValue(java.lang.String s);

    void setColumnValue(java.lang.String s, java.lang.Object o);

    tosa.api.IDBObject getFkValue(java.lang.String s);

    void setFkValue(java.lang.String s, tosa.api.IDBObject idbObject);

    tosa.api.EntityCollection getArray(java.lang.String s);

    tosa.api.EntityCollection getArray(tosa.api.IDBArray idbArray);

    java.lang.Long getId();

    java.lang.Long toID();

    boolean isNew();

    void update();

    void delete();
  }
}
