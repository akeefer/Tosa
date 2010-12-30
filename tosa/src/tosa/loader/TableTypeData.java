package tosa.loader;

import tosa.Join;
import tosa.loader.data.ColumnData;
import tosa.loader.data.TableData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/30/10
 * Time: 1:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class TableTypeData {
  private TableData _tableData;
  private boolean _hasId;
  private boolean _isJoinTable;
  private String _joinName;
  private String _firstJoinTable;
  private String _secondJoinTable;

  private List<ColumnTypeData> _columns;

  // All the tables that this table is joined with
  private List<Join> _joins;
  // Tables that have FKs to this table
  private List<String> _incomingFKs;

  public TableTypeData(TableData tableData) {
    _tableData = tableData;
    _joins = new ArrayList<Join>();
    _incomingFKs = new ArrayList<String>();
    _columns = new ArrayList<ColumnTypeData>();

    initializeBasicProperties();
    initializeColumns();
  }

  private void initializeBasicProperties() {
    String tableName = _tableData.getName();
    if (tableName.contains("join_")) {
      _isJoinTable = true;
      if (!tableName.startsWith("join_")) {
        _joinName = tableName.substring(0, tableName.indexOf('_'));
      }
      int lastUnderscore = tableName.lastIndexOf('_');
      int nextToLastUnderscore = tableName.lastIndexOf('_', lastUnderscore - 1);
      _firstJoinTable = tableName.substring(nextToLastUnderscore + 1, lastUnderscore);
      _secondJoinTable = tableName.substring(lastUnderscore + 1);
    }
  }

  private void initializeColumns() {
    for (ColumnData column : _tableData.getColumns()) {
      ColumnTypeData columnTypeData = new ColumnTypeData(column);
      if (columnTypeData.isIdColumn()) {
        _hasId = true;
      }
      _columns.add(columnTypeData);
    }
  }

  public List<ColumnTypeData> getColumns() {
    // TODO - AHK - This should be an immutable list
    return _columns;
  }

  public String getTableName() {
    return _tableData.getName();
  }

  public boolean hasId() {
    return _hasId;
  }

  public boolean isJoinTable() {
    return _isJoinTable;
  }

  public String getJoinName() {
    return _joinName;
  }

  public String getFirstJoinTable() {
    return _firstJoinTable;
  }

  public String getSecondJoinTable() {
    return _secondJoinTable;
  }

  public void addJoin(Join join) {
    _joins.add(join);
  }

  public void addIncomingFK(String referencingTable) {
    _incomingFKs.add(referencingTable);
  }
}
