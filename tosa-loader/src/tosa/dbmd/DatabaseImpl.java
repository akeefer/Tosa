package tosa.dbmd;

import gw.fs.IFile;
import gw.lang.reflect.module.IModule;
import tosa.DBConnection;
import tosa.api.*;
import tosa.db.execution.DBExecutionKernelImpl;
import tosa.db.execution.DBUpgraderImpl;
import tosa.impl.md.DBFkArrayImpl;
import tosa.impl.md.DBJoinArrayImpl;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
  private DBConnection _connection;
  private final DBExecutionKernelImpl _executionKernel;
  private String _jdbcUrl;
  private IModule _module;

  public DatabaseImpl(String namespace, DBData dbData, IModule module) {
    _namespace = namespace;
    _dbData = dbData;
    _module = module;
    Map<String, DBTableImpl> tables = new HashMap<String, DBTableImpl>();

    processDBData(tables);

    _tables = Collections.unmodifiableMap(tables);

    _connection = null;
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

  // TODO - AHK - Synchronization, since this thing is modifiable

  @Override
  public IDBConnection getConnection() {
    if ( _connection == null ) {
      throw new IllegalStateException("Database Connection is null.  Please ensure your JdbcUrl is set properly.");
    }
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
        String firstTableName = tableName.substring(nextToLastUnderscore + 1, lastUnderscore);
        String secondTableName = tableName.substring(lastUnderscore + 1);
        DBTableImpl firstTable = tables.get(firstTableName);
        DBTableImpl secondTable = tables.get(secondTableName);

        IDBColumn firstFkColumn;
        IDBColumn secondFkColumn;
        // TODO - AHK - Validate that the columns aren't null
        if (firstTable.equals(secondTable)) {
          firstFkColumn = table.getColumn(firstTableName + "_src_id");
          secondFkColumn = table.getColumn(firstTableName + "_dest_id");
        } else {
          firstFkColumn = table.getColumn(firstTableName + "_id");
          secondFkColumn = table.getColumn(secondTableName + "_id");
        }
        // TODO - AHK - Handle the case where the tables are null
        // TODO - AHK - Put join name computation in a method
        firstTable.addArray(new DBJoinArrayImpl(joinName == null ? secondTableName + "s" : joinName, firstTable, secondTable, table, firstFkColumn, secondFkColumn));
        if (!firstTable.equals(secondTable)) {
          secondTable.addArray(new DBJoinArrayImpl(joinName == null ? firstTableName + "s" : joinName, secondTable, firstTable, table, secondFkColumn, firstFkColumn));
        }
      } else {
        for (DBColumnImpl column : table.getColumns()) {
          if (column.isFK()) {
            String fkTargetName = column.getFKTargetName();
            DBTableImpl dbTable = tables.get(fkTargetName);
            if (dbTable != null) {
              dbTable.addArray(new DBFkArrayImpl(computeFkArrayPropertyName(column), dbTable, table, column));
              dbTable.addIncomingFK(column);
            } else {
              // TODO cgross - how to handle this situation?
              System.out.println("No table found with name " + fkTargetName);
            }
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

  private String computeFkArrayPropertyName(IDBColumn fkColumn) {
    // TODO - AHK - This algorithm probably needs to be a bit more complicated . . .
    return fkColumn.getTable().getName() + "s";
  }

  @Override
  public String getJdbcUrl() {
    return _jdbcUrl;
  }

  @Override
  public void setJdbcUrl(String url) {
    if (url == null) {
      throw new IllegalArgumentException("The jdbc url for a Tosa database cannot be nulled out");
    }
    // TODO - AHK - Other sorts of validation?
    // TODO - AHK - Check for MySql ANSI_QUOTES string?
    _jdbcUrl = url;
    // TODO - AHK - Synchronization
    _connection = new DBConnection(url, _module);
  }

  @Override
  public void createTables() {
    getDBUpgrader().createTables();
  }

  @Override
  public void dropTables() {
    getDBUpgrader().dropTables();
  }
}
