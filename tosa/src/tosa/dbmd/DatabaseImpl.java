package tosa.dbmd;

import tosa.DBConnection;
import tosa.Join;
import tosa.api.IDBColumn;
import tosa.api.IDatabase;
import tosa.api.IPreparedStatementParameter;
import tosa.loader.DBTypeLoader;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;

import java.sql.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

  public String getNamespace() {
    return _namespace;
  }

  public DBTableImpl getTable(String tableName) {
    return _tables.get(tableName);
  }

  public Collection<DBTableImpl> getAllTables() {
    return _tables.values();
  }

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


  public IPreparedStatementParameter wrapParameter(Object value, IDBColumn column) {
    // TODO - AHK - Do data conversions here
    return new PreparedStatementParameterImpl(value, column.getColumnType().getJdbcTypeNumber());
  }

  public Object executeInsert(String sql, IPreparedStatementParameter... arguments) {
    Object generatedKey = null;
    try {
      Connection connection = _connection.connect();
      try {
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < arguments.length; i++) {
          arguments[i].setParameter(statement, i + 1);
        }
        try {
          statement.execute();
          ResultSet result = statement.getGeneratedKeys();
          try {
            if (result.first()) {
              generatedKey = result.getObject(1);
            }
          } finally {
            result.close();
          }
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

    return generatedKey;
  }


//  public void executeInsert(String sql, Object[] arguments) {
//    Connection connection = _connection.connect();
//    try {
//      PreparedStatement statement = connection.prepareStatement(sql);
//      for (int i = 0; i < arguments.length; i++) {
//        statement.setObject(i + 1, arguments[i]);
//      }
//      try {
//        statement.
//      } finally {
//        statement.close();
//      }
//    } finally {
//      connection.close();
//    }
//  }
//
//  private void setParameter(PreparedStatement statement, int position, Object argument) {
//    statement.setN
//  }
}
