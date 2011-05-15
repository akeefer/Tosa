package tosa.api;

import gw.lang.reflect.IType;
import gw.lang.reflect.gs.IGosuObject;

import java.sql.SQLException;

/**
 * IDBObject is the interface implemented by all database objects in Tosa.  It provides basic operations
 * that can be performed on the object, such as getting and setting column values, updating, and deleting
 * the object.
 */
public interface IDBObject extends IGosuObject {

  IDBTable getDBTable();

  Object getColumnValue(String columnName);

  void setColumnValue(String columnName, Object value);

  IDBObject getFkValue(String columnName);

  void setFkValue(String columnName, IDBObject value);

  Long getId();

  boolean isNew();

  void update() throws SQLException;

  void delete() throws SQLException;
}
