package tosa.impl;

import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.api.IPreparedStatementParameter;
import tosa.loader.IDBType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/8/11
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
// TODO - This class likely needs a different name
public interface QueryExecutor {

  int count(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters);

  List<IDBObject> selectEntity(String profilerTag, IDBType targetType, String sqlStatement, IPreparedStatementParameter... parameters);

  void update(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters);

  void insert(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters);
}
