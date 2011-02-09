package tosa.api;

import gw.lang.reflect.IType;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/8/11
 * Time: 8:32 PM
 * To change this template use File | Settings | File Templates.
 */
// TODO - AHK - Decide if this particular name actually makes sense
public interface IDBColumnType {
  // TODO - AHK - Do we need both a Name and a Description?
  String getName();
  String getDescription();
  String getGosuTypeName();
  IType getGosuType();
  int getJdbcType();
  // TODO - AHK - Validation
  // TODO - AHK - PreparedStatement/ResultSet handling
  // TODO - AHK - Constraints (length, scale, precision, fk constraints, unique indexes, etc.)
}
