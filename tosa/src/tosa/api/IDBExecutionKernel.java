package tosa.api;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/18/11
 * Time: 8:34 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBExecutionKernel {

  // TODO - AHK - Decide on packaging

  // TODO - AHK - Decide if this is really a good API

    // TODO - AHK - This should probably return more than just one object
  Object executeInsert(String sql, IPreparedStatementParameter... arguments);

  <T> List<T> executeSelect(String sql, IQueryResultProcessor<T> resultProcessor, IPreparedStatementParameter... arguments);

  void executeUpdate(String sql, IPreparedStatementParameter... arguments);

  void executeDelete(String sql, IPreparedStatementParameter... arguments);

}
