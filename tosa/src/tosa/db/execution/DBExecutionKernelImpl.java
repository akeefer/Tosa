package tosa.db.execution;

import tosa.api.IDBExecutionKernel;
import tosa.api.IDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/18/11
 * Time: 8:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class DBExecutionKernelImpl implements IDBExecutionKernel {

  private IDatabase _database;

  public DBExecutionKernelImpl(IDatabase database) {
    _database = database;
  }

  @Override
  public Object executeInsert(String sql, IPreparedStatementParameter... arguments) {
    InsertExecuteCallback callback = new InsertExecuteCallback();
    execute(sql, arguments, callback);
    return callback.getGeneratedKey();
  }

  @Override
  public <T> List<T> executeSelect(String sql, IQueryResultProcessor<T> resultProcessor, IPreparedStatementParameter... arguments) {
    SelectExecuteCallback<T> callback = new SelectExecuteCallback<T>(resultProcessor);
    execute(sql, arguments, callback);
    return callback.getResults();
  }

  @Override
  public void executeUpdate(String sql, IPreparedStatementParameter... arguments) {
    UpdateExecuteCallback callback = new UpdateExecuteCallback();
    execute(sql, arguments, callback);
  }

  @Override
  public void executeDelete(String sql, IPreparedStatementParameter... arguments) {
    execute(sql, arguments, new DeleteExecuteCallback());
  }

  private static class InsertExecuteCallback implements ExecuteCallback {

    private Object _generatedKey;

    @Override
    public PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException {
      return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    @Override
    public void processStatementPostExecute(PreparedStatement statement) throws SQLException {
      ResultSet result = statement.getGeneratedKeys();
      try {
        if (result.first()) {
          _generatedKey = result.getObject(1);
        }
      } finally {
        result.close();
      }
    }

    public Object getGeneratedKey() {
      return _generatedKey;
    }
  }

  private static class UpdateExecuteCallback implements ExecuteCallback {

    @Override
    public PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException {
      return connection.prepareStatement(sql);
    }

    @Override
    public void processStatementPostExecute(PreparedStatement statement) throws SQLException {
    }
  }

  private static class SelectExecuteCallback<T> implements ExecuteCallback {
    private IQueryResultProcessor<T> _processor;
    private List<T> _results;

    private SelectExecuteCallback(IQueryResultProcessor<T> processor) {
      _processor = processor;
    }

    @Override
    public PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException {
      return connection.prepareStatement(sql);
    }

    @Override
    public void processStatementPostExecute(PreparedStatement statement) throws SQLException {
      // TODO - AHK - Should this be created up-front so it's non-null if an exception occurs somewhere?
      _results = new ArrayList<T>();
      ResultSet result = statement.getResultSet();
      try {
        if (result.first()) {
          while (!result.isAfterLast()) {
            _results.add(_processor.processResult(result));
            result.next();
          }
          result.next();
        }
      } finally {
        result.close();
      }
    }

    public List<T> getResults() {
      return _results;
    }
  }

  private static class DeleteExecuteCallback implements ExecuteCallback {
    @Override
    public PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException {
      return connection.prepareStatement(sql);
    }

    @Override
    public void processStatementPostExecute(PreparedStatement statement) throws SQLException {
      // Nothing to do here
    }
  }

  private interface ExecuteCallback {
    PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException;
    void processStatementPostExecute(PreparedStatement statement) throws SQLException;
  }

  private void execute(String sql, IPreparedStatementParameter[] arguments, ExecuteCallback callback) {
    try {
      Connection connection = _database.getConnection().connect();
      try {
        PreparedStatement statement = callback.prepareStatement(connection, sql);
        for (int i = 0; i < arguments.length; i++) {
          arguments[i].setParameter(statement, i + 1);
        }
        try {
          statement.execute();
          callback.processStatementPostExecute(statement);
        } catch (SQLException e) {
          // TODO - AHK - Handle the error better
          throw new RuntimeException(e);
        } finally {
          statement.close();
        }
      } catch (SQLException e) {
        // TODO - AHK - Handle the error better
          throw new RuntimeException(e);
      } finally {
        connection.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
