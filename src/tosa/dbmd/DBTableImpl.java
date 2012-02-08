package tosa.dbmd;

import tosa.api.IDBArray;
import tosa.api.IDBColumn;
import tosa.api.IDBTable;
import tosa.api.IDatabase;
import tosa.loader.data.ColumnData;
import tosa.loader.data.TableData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/30/10
 * Time: 1:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class DBTableImpl implements IDBTable {
  // TODO - AHK - Make stuff final
  private final DatabaseImpl _database;
  private final TableData _tableData;
  private final boolean _hasId;
  private final List<DBColumnImpl> _columns;
  private final List<IDBArray> _arrays;
  // Tables that have FKs to this table
  private final List<IDBColumn> _incomingFKs;

  public DBTableImpl(DatabaseImpl database, TableData tableData) {
    _database = database;
    _tableData = tableData;
    _arrays = new ArrayList<IDBArray>();
    _incomingFKs = new ArrayList<IDBColumn>();

    // It might be best to do this in a separate method, but that gets annoying with Java rules
    // around when final variables can be initialized
    List<DBColumnImpl> columns = new ArrayList<DBColumnImpl>();
    boolean hasId = false;
    for (ColumnData column : _tableData.getColumns()) {
      DBColumnImpl columnTypeData = new DBColumnImpl(this, column);
      if (columnTypeData.isIdColumn()) {
        hasId = true;
      }
      columns.add(columnTypeData);
    }
    _columns = Collections.unmodifiableList(columns);
    _hasId = hasId;

    // TODO - AHK - Is there any good way to make the list of joins/fks immutable?
  }

  @Override
  public String getName() {
    return _tableData.getName();
  }

  @Override
  public String getPossiblyQuotedName() {
    return _tableData.getPossiblyQuotedName();
  }

  @Override
  public IDatabase getDatabase() {
    return _database;
  }

  @Override
  public IDBColumn getColumn(String name) {
    // TODO - AHK - Use a hash table if it matters
    for (IDBColumn column : _columns) {
      if (column.getName().equals(name)) {
        return column;
      }
    }
    return null;
  }

  public List<DBColumnImpl> getColumns() {
    return _columns;
  }

  @Override
  public IDBArray getArray(String propertyName) {
    // TODO - AHK - Use a hash table if it matters
    for (IDBArray array : _arrays) {
      if (propertyName.equals(array.getPropertyName())) {
        return array;
      }
    }

    return null;
  }

  @Override
  public Collection<? extends IDBArray> getArrays() {
    return _arrays;
  }

  public boolean hasId() {
    return _hasId;
  }

  void addArray(IDBArray array) {
    _arrays.add(array);
  }

  // TODO - AHK - Kill this if possible
  void addIncomingFK(IDBColumn fkColumn) {
    _incomingFKs.add(fkColumn);
  }

  public List<? extends IDBColumn> getIncomingFKs() {
    return _incomingFKs;
  }
}
