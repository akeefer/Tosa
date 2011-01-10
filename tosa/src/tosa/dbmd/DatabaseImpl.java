package tosa.dbmd;

import tosa.DBConnection;
import tosa.Join;
import tosa.api.IDatabase;
import tosa.loader.DBTypeLoader;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;

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
  private final Set<String> _typeNames;
  private final DBConnection _connection;

  public DatabaseImpl(String namespace, DBData dbData, DBTypeLoader typeLoader) {
    _namespace = namespace;
    _dbData = dbData;
    Map<String, DBTableImpl> tables = new HashMap<String, DBTableImpl>();
    Set<String> typeNames = new HashSet<String>();

    processDBData(tables, typeNames);

    _tables = Collections.unmodifiableMap(tables);
    _typeNames = Collections.unmodifiableSet(typeNames);
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

  public Set<String> getTypeNames() {
    return _typeNames;
  }

  public DBConnection getConnection() {
    return _connection;
  }

  private void processDBData(Map<String, DBTableImpl> tables, Set<String> typeNames) {
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
        tables.get(firstTable).addJoin(new Join(joinName == null ? secondTable + "s" : joinName, secondTable, table.getName()));
        if (!firstTable.equals(secondTable)) {
          tables.get(secondTable).addJoin(new Join(joinName == null ? firstTable + "s" : joinName, firstTable, table.getName()));
        }
      } else {
        typeNames.add(_namespace + "." + table.getName());
        for (DBColumnImpl column : table.getColumns()) {
          if (column.isFK()) {
            tables.get(column.getFkTarget()).addIncomingFK(table.getName());
          }
        }
      }
    }
  }


}
