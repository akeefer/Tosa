package tosa.loader;

import tosa.Join;
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
public class DBTypeData {

  private final String _namespace;
  private final DBData _dbData;
  private final Map<String, TableTypeData> _tables;
  private final Set<String> _typeNames;

  public DBTypeData(String namespace, DBData dbData) {
    _namespace = namespace;
    _dbData = dbData;
    Map<String, TableTypeData> tables = new HashMap<String, TableTypeData>();
    Set<String> typeNames = new HashSet<String>();

    processDBData(tables, typeNames);

    _tables = Collections.unmodifiableMap(tables);
    _typeNames = Collections.unmodifiableSet(typeNames);
  }

  public String getNamespace() {
    return _namespace;
  }

  public TableTypeData getTable(String tableName) {
    return _tables.get(tableName);
  }

  public Collection<TableTypeData> getAllTables() {
    return _tables.values();
  }

  public Set<String> getTypeNames() {
    return _typeNames;
  }

  private void processDBData(Map<String, TableTypeData> tables, Set<String> typeNames) {
    // Create the initial set of objects
    for (TableData table : _dbData.getTables()) {
      tables.put(table.getName(), new TableTypeData(this, table));
    }

    // Now link together the various join and fk relationships
    for (TableData table : _dbData.getTables()) {
      TableTypeData tableTypeData = tables.get(table.getName());
      if (tableTypeData.isJoinTable()) {
        String joinName = tableTypeData.getJoinName();
        String firstTable = tableTypeData.getFirstJoinTable();
        String secondTable = tableTypeData.getSecondJoinTable();
        tables.get(firstTable).addJoin(new Join(joinName == null ? secondTable + "s" : joinName, secondTable, tableTypeData.getTableName()));
        if (!firstTable.equals(secondTable)) {
          tables.get(secondTable).addJoin(new Join(joinName == null ? firstTable + "s" : joinName, firstTable, tableTypeData.getTableName()));
        }
      } else {
        typeNames.add(_namespace + "." + tableTypeData.getTableName());
        for (ColumnTypeData column : tableTypeData.getColumns()) {
          if (column.isFK()) {
            tables.get(column.getFkTarget()).addIncomingFK(table.getName());
          }
        }
      }
    }
  }
}
