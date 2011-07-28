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

  // TODO - AHK - Rename to get_DBTable?
  IDBTable getDBTable();

  Object getColumnValue(String columnName);

  void setColumnValue(String columnName, Object value);

  IDBObject getFkValue(String columnName);

  void setFkValue(String columnName, IDBObject value);

  EntityCollection getArray(String arrayName);

  EntityCollection getArray(IDBArray dbArray);

  Long getId();

  // TODO - AHK - Rename this?
  Long toID();

  boolean isNew();

  void update();

  void delete();
}
