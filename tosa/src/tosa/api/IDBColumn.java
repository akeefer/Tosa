package tosa.api;

import tosa.loader.data.DBColumnTypeImpl;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/9/11
 * Time: 4:31 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBColumn {

  IDBTable getTable();

  String getName();

  boolean isFK();

  IDBTable getFKTarget();

  // TODO - AHK - Pull the column type into the api package
  IDBColumnType getColumnType();
  // TODO - Column type
  // TODO - Additional attributes
}
