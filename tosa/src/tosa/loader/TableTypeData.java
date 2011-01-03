package tosa.loader;

import tosa.Join;
import tosa.loader.data.ColumnData;
import tosa.loader.data.TableData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/30/10
 * Time: 1:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class TableTypeData {
  // TODO - AHK - Make stuff final
  private final DBTypeData _dbTypeData;
  private final TableData _tableData;
  private final boolean _hasId;
  private final boolean _isJoinTable;
  private final String _joinName;
  private final String _firstJoinTable;
  private final String _secondJoinTable;

  private final List<ColumnTypeData> _columns;

  // All the tables that this table is joined with
  private final List<Join> _joins;
  // Tables that have FKs to this table
  private final List<String> _incomingFKs;

  public TableTypeData(DBTypeData dbTypeData, TableData tableData) {
    _dbTypeData = dbTypeData;
    _tableData = tableData;
    _joins = new ArrayList<Join>();
    _incomingFKs = new ArrayList<String>();

    // It might be best to do this in a separate method, but that gets annoying with Java rules
    // around when final variables can be initialized
    String tableName = _tableData.getName();
    if (tableName.contains("join_")) {
      _isJoinTable = true;
      if (!tableName.startsWith("join_")) {
        _joinName = tableName.substring(0, tableName.indexOf('_'));
      } else {
        _joinName = null;
      }
      int lastUnderscore = tableName.lastIndexOf('_');
      int nextToLastUnderscore = tableName.lastIndexOf('_', lastUnderscore - 1);
      _firstJoinTable = tableName.substring(nextToLastUnderscore + 1, lastUnderscore);
      _secondJoinTable = tableName.substring(lastUnderscore + 1);
    } else {
      _isJoinTable = false;
      _joinName = null;
      _firstJoinTable = null;
      _secondJoinTable = null;
    }

    // It might be best to do this in a separate method, but that gets annoying with Java rules
    // around when final variables can be initialized
    List<ColumnTypeData> columns = new ArrayList<ColumnTypeData>();
    boolean hasId = false;
    for (ColumnData column : _tableData.getColumns()) {
      ColumnTypeData columnTypeData = new ColumnTypeData(this, column);
      if (columnTypeData.isIdColumn()) {
        hasId = true;
      }
      columns.add(columnTypeData);
    }
    _columns = Collections.unmodifiableList(columns);
    _hasId = hasId;

    // TODO - AHK - Is there any good way to make the list of joins/fks immutable?
  }

  public DBTypeData getDbTypeData() {
    return _dbTypeData;
  }

  public List<ColumnTypeData> getColumns() {
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

  // TODO - AHK - Kill this if possible
  void addJoin(Join join) {
    _joins.add(join);
  }

  // TODO - AHK - Kill this if possible
  void addIncomingFK(String referencingTable) {
    _incomingFKs.add(referencingTable);
  }

  public List<Join> getJoins() {
    return _joins;
  }

  public List<String> getIncomingFKs() {
    return _incomingFKs;
  }
}
