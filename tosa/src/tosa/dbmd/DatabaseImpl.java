package tosa.dbmd;

import tosa.DBConnection;
import tosa.Join;
import tosa.api.IDBColumn;
import tosa.api.IDatabase;
import tosa.loader.DBTypeLoader;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;

import java.sql.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/30/10
 * Time: 1:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseImpl implements IDatabase {

  private final String _namespace;
  private final DBData _dbData;
  private final Map<String, DBTableImpl> _tables;
  private final DBConnection _connection;

  public DatabaseImpl(String namespace, DBData dbData, DBTypeLoader typeLoader) {
    _namespace = namespace;
    _dbData = dbData;
    Map<String, DBTableImpl> tables = new HashMap<String, DBTableImpl>();

    processDBData(tables);

    _tables = Collections.unmodifiableMap(tables);
    _connection = new DBConnection(dbData.getConnectionString(), typeLoader);
  }

  @Override
  public String getNamespace() {
    return _namespace;
  }

  @Override
  public DBTableImpl getTable(String tableName) {
    return _tables.get(tableName);
  }

  @Override
  public Collection<DBTableImpl> getAllTables() {
    return _tables.values();
  }

  @Override
  public DBConnection getConnection() {
    return _connection;
  }

  private void processDBData(Map<String, DBTableImpl> tables) {
    // Create the initial set of objects
    for (TableData table : _dbData.getTables()) {
      tables.put(table.getName(), new DBTableImpl(this, table));
    }

    // Now link together the various join and fk relationships
    for (TableData tableData : _dbData.getTables()) {
      DBTableImpl table = tables.get(tableData.getName());

      String tableName = tableData.getName();
      if (tableName.contains("join_")) {
        String joinName = null;
        if (!tableName.startsWith("join_")) {
          joinName = tableName.substring(0, tableName.indexOf('_'));
        }
        int lastUnderscore = tableName.lastIndexOf('_');
        int nextToLastUnderscore = tableName.lastIndexOf('_', lastUnderscore - 1);
        String firstTable = tableName.substring(nextToLastUnderscore + 1, lastUnderscore);
        String secondTable = tableName.substring(lastUnderscore + 1);
        tables.get(firstTable).addJoin(new Join(joinName == null ? secondTable + "s" : joinName, tables.get(secondTable), table));
        if (!firstTable.equals(secondTable)) {
          tables.get(secondTable).addJoin(new Join(joinName == null ? firstTable + "s" : joinName, tables.get(firstTable), table));
        }
      } else {
        for (DBColumnImpl column : table.getColumns()) {
          if (column.isFK()) {
            tables.get(column.getFKTargetName()).addIncomingFK(column);
          }
        }
      }
    }
  }


  @Override
  public IPreparedStatementParameter wrapParameter(Object value, IDBColumn column) {
    // TODO - AHK - Do data conversions here
    return new PreparedStatementParameterImpl(value, column.getColumnType().getJdbcType());
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
      Connection connection = _connection.connect();
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
