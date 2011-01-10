package tosa.dbmd;

import tosa.Join;
import tosa.api.IDBTable;
import tosa.api.IDatabase;
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
public class DBTableImpl implements IDBTable {
  // TODO - AHK - Make stuff final
  private final DatabaseImpl _database;
  private final TableData _tableData;
  private final boolean _hasId;
  private final List<DBColumnImpl> _columns;

  // All the tables that this table is joined with
  private final List<Join> _joins;
  // Tables that have FKs to this table
  private final List<IDBTable> _incomingFKs;

  public DBTableImpl(DatabaseImpl database, TableData tableData) {
    _database = database;
    _tableData = tableData;
    _joins = new ArrayList<Join>();
    _incomingFKs = new ArrayList<IDBTable>();

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
  public IDatabase getDatabase() {
    return _database;
  }

  public List<DBColumnImpl> getColumns() {
    return _columns;
  }

  public boolean hasId() {
    return _hasId;
  }

  // TODO - AHK - Kill this if possible
  void addJoin(Join join) {
    _joins.add(join);
  }

  // TODO - AHK - Kill this if possible
  void addIncomingFK(IDBTable referencingTable) {
    _incomingFKs.add(referencingTable);
  }

  public List<Join> getJoins() {
    return _joins;
  }

  public List<IDBTable> getIncomingFKs() {
    return _incomingFKs;
  }
}
