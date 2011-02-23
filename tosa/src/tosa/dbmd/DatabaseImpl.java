package tosa.dbmd;

import gw.fs.IFile;
import tosa.DBConnection;
import tosa.Join;
import tosa.api.*;
import tosa.db.execution.DBExecutionKernelImpl;
import tosa.db.execution.DBUpgraderImpl;
import tosa.loader.DBTypeLoader;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;

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
  private final DBExecutionKernelImpl _executionKernel;

  public DatabaseImpl(String namespace, DBData dbData, DBTypeLoader typeLoader) {
    _namespace = namespace;
    _dbData = dbData;
    Map<String, DBTableImpl> tables = new HashMap<String, DBTableImpl>();

    processDBData(tables);

    _tables = Collections.unmodifiableMap(tables);
    _connection = new DBConnection(dbData.getConnectionString(), typeLoader);
    _executionKernel = new DBExecutionKernelImpl(this);
  }

  @Override
  public String getNamespace() {
    return _namespace;
  }

  @Override
  public IDBTable getTable(String tableName) {
    return _tables.get(tableName);
  }

  @Override
  public Collection<DBTableImpl> getAllTables() {
    return _tables.values();
  }

  public DBData getDBData() {
    return _dbData;
  }

  @Override
  public IDBConnection getConnection() {
    return _connection;
  }

  @Override
  public IDBExecutionKernel getDBExecutionKernel() {
    return _executionKernel;
  }

  @Override
  public IDBUpgrader getDBUpgrader() {
    // TODO - AHK - Should we create this every time, or keep a handle to it?
    return new DBUpgraderImpl(this);
  }

  public IFile getDdlFile() {
    return _dbData.getDdlFile();
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


}
