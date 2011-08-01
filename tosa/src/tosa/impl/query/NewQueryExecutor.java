package tosa.impl.query;

import tosa.api.IDBObject;
import tosa.api.IPreparedStatementParameter;
import tosa.api.QueryResult;
import tosa.loader.IDBType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 7/31/11
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface NewQueryExecutor {

  QueryResult<IDBObject> selectEntity(String profilerTag, IDBType targetType, String sqlStatement, Object... parameters);
}
