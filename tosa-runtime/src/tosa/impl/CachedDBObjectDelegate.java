package tosa.impl;

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/9/12
 * Time: 3:52 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CachedDBObjectDelegate {
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
