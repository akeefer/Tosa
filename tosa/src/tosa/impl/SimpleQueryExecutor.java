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
public interface SimpleQueryExecutor {

  // TODO - AHK - Should these be in terms of IDBTable or IDBType?
  int countWhere(String profilerTag, IDBType targetType, String whereClause, IPreparedStatementParameter... parameters);

  int count(String profilerTag, IDBType targetType, String sqlStatement, IPreparedStatementParameter... parameters);

  List<IDBObject> find(String profilerTag, IDBType targetType, String sqlStatement, IPreparedStatementParameter... parameters);
}
