package tosa.impl;

import gw.lang.reflect.IType;
import gw.lang.reflect.ReflectUtil;
import gw.util.GosuRefactorUtil;
import tosa.api.EntityCollection;
import tosa.api.IDBArray;
import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.loader.DBTypeInfo;
import tosa.loader.IDBType;

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/9/12
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CachedDBObject implements IDBObject {

  private IDBType _type;
  private CachedDBObjectDelegate _delegate;
  
  public CachedDBObject(IDBType type, boolean isNew) {
    _type = type;
    _delegate = createDelegate(type, isNew);
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

  private CachedDBObjectDelegate createDelegate(IDBType type, boolean isNew) {
    // TODO - AHK - Optimize this
    return ReflectUtil.construct("tosa.impl.CachedDBObjectDelegateImpl", type, isNew, this);
  }
}
