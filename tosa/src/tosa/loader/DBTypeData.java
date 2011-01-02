package tosa.loader;

import tosa.Join;
import tosa.loader.data.DBData;
import tosa.loader.data.TableData;

import java.util.Collection;
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
  private String _namespace;
  private DBData _dbData;
  private Map<String, TableTypeData> _tables;
  private Set<String> _typeNames;

  public DBTypeData(String namespace, DBData dbData) {
    _namespace = namespace;
    _dbData = dbData;
    _tables = new HashMap<String, TableTypeData>();
    _typeNames = new HashSet<String>();

    processDBData();
  }

  public String getNamespace() {
    return _namespace;
  }

  public TableTypeData getTable(String tableName) {
    return _tables.get(tableName);
  }

  public Set<String> getTypeNames() {
    // TODO - AHK - Make sure it's unmodifiable
    return _typeNames;
  }

  private void processDBData() {
    // Create the initial set of objects
    for (TableData table : _dbData.getTables()) {
      _tables.put(table.getName(), new TableTypeData(table));
    }

    // Now link together the various join and fk relationships
    for (TableData table : _dbData.getTables()) {
      TableTypeData tableTypeData = _tables.get(table.getName());
      if (tableTypeData.isJoinTable()) {
        String joinName = tableTypeData.getJoinName();
        String firstTable = tableTypeData.getFirstJoinTable();
        String secondTable = tableTypeData.getSecondJoinTable();
        _tables.get(firstTable).addJoin(new Join(joinName == null ? secondTable + "s" : joinName, secondTable, tableTypeData.getTableName()));
        if (!firstTable.equals(secondTable)) {
          _tables.get(secondTable).addJoin(new Join(joinName == null ? firstTable + "s" : joinName, firstTable, tableTypeData.getTableName()));
        }
      } else {
        _typeNames.add(_namespace + "." + tableTypeData.getTableName());
        for (ColumnTypeData column : tableTypeData.getColumns()) {
          if (column.isFK()) {
            _tables.get(column.getFkTarget()).addIncomingFK(table.getName());
          }
        }
      }
    }
  }
}
