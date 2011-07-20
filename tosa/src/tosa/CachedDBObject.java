package tosa;

import gw.lang.reflect.TypeSystem;
import tosa.api.*;
import tosa.impl.*;
import tosa.loader.DBTypeInfo;
import tosa.loader.IDBType;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CachedDBObject implements IDBObject {
  private Map<String, Object> _columns;
  private Map<String, IDBObject> _cachedFks;
  private Map<String, EntityCollection> _cachedArrays;
  private IDBType _type;
  private boolean _new;
  private QueryExecutor _queryExecutor;

  public CachedDBObject(IDBType type, boolean isNew) {
    // TODO - AHK
    _type = (IDBType) TypeSystem.getOrCreateTypeReference(type);
    _new = isNew;
    _columns = new HashMap<String, Object>();
    _cachedFks = new HashMap<String, IDBObject>();
    _cachedArrays = new HashMap<String, EntityCollection>();
    _queryExecutor = new QueryExecutorImpl(_type.getTable().getDatabase());
    // TODO - AHK - There's room for perf improvements here
  }

  @Override
  public IDBType getIntrinsicType() {
    return _type;
  }

  @Override
  public IDBTable getDBTable() {
    return _type.getTable();
  }

  private String getTableName() {
    return getDBTable().getName();
  }

  @Override
  public boolean isNew() {
    return _new;
  }

  @Override
  public Object getColumnValue(String columnName) {
    // TODO - AHK - Validate that the column name is actually a legal column
    return _columns.get(columnName);
  }

  @Override
  public void setColumnValue(String columnName, Object value) {
    // TODO - AHK - Validate that the column name is legal and that the value is legal
    // TODO - AHK - Invalidate any fk back-pointers associated with that column if the value has changed
    _columns.put(columnName, value);
  }

  @Override
  public IDBObject getFkValue(String columnName) {
    IDBColumn column = getAndValidateFkColumn(columnName);

    IDBObject fkObject = _cachedFks.get(columnName);
    if (fkObject != null) {
      return fkObject;
    }

    Long fkID = (Long) _columns.get(columnName);
    if (fkID == null) {
      return null;
    }

    fkObject = loadEntity(column.getFKTarget(), fkID);
    if (fkObject == null) {
      throw new IllegalStateException("Column " + columnName + " on table " + _type.getTable().getName() + " has a value of " + fkID + ", but no corresponding row was found in the database");
    }

    return fkObject;
  }

  @Override
  public void setFkValue(String columnName, IDBObject value) {
    IDBColumn column = getAndValidateFkColumn(columnName);
    // TODO - AHK - Validate that the value is of the correct type
    if (value == null) {
      _columns.put(columnName, null);
      _cachedFks.put(columnName, null);
    } else {
      _columns.put(columnName, value.getId());
      _cachedFks.put(columnName, value);
    }
  }

  private IDBColumn getAndValidateFkColumn(String columnName) {
    IDBColumn column = _type.getTable().getColumn(columnName);
    if (column == null) {
      throw new IllegalArgumentException("Column name " + columnName + " is not a valid column on the  " + _type.getTable().getName() + " table");
    }
    if (!column.isFK()) {
      throw new IllegalArgumentException("Column " + columnName + " on table " + _type.getTable().getName() + " is not a foreign key");
    }
    return column;
  }

  public EntityCollection getArray(String arrayName) {
    // TODO - AHK - Validate it
    return getArray(_type.getTable().getArray(arrayName));
  }

  @Override
  public EntityCollection getArray(IDBArray dbArray) {
    EntityCollection result = _cachedArrays.get(dbArray.getPropertyName());
    if (result == null) {
      if (dbArray instanceof IDBFkArray) {
        IDBColumn fkColumn = ((IDBFkArray) dbArray).getFkColumn();
        IDBType fkType = (IDBType) TypeSystem.getByFullName(fkColumn.getTable().getDatabase().getNamespace() + "." + fkColumn.getTable().getName());
        result = new ReverseFkEntityCollectionImpl(this, fkType, fkColumn, new QueryExecutorImpl(fkColumn.getTable().getDatabase()));
      } else if (dbArray instanceof IDBJoinArray) {
        IDBJoinArray joinArray = (IDBJoinArray) dbArray;
        IDBType targetType = (IDBType) TypeSystem.getByFullName(getDBTable().getDatabase().getNamespace() + "." + joinArray.getTargetTable().getName());
        result = new JoinArrayEntityCollectionImpl(this, targetType, joinArray.getSrcColumn(), joinArray.getTargetColumn(), new QueryExecutorImpl(getDBTable().getDatabase()));
      }
      _cachedArrays.put(dbArray.getPropertyName(), result);
    }
    return result;
  }

  @Override
  public Long getId() {
    return (Long) getColumnValue(DBTypeInfo.ID_COLUMN);
  }

  // TODO - AHK - Kill this
  public Map<String, Object> getColumns() {
    return _columns;
  }

  @Override
  public void update() throws SQLException {
    List<ColumnValuePair> columnValues = gatherChangedValues();
    if (_new) {
      List<IDBColumn> columns = new ArrayList<IDBColumn>();
      List<String> values = new ArrayList<String>();
      IPreparedStatementParameter[] parameters = new IPreparedStatementParameter[columnValues.size()];
      for (int i = 0; i < columnValues.size(); i++) {
        columns.add(columnValues.get(i)._column);
        values.add("?");
        parameters[i] = columnValues.get(i)._parameter;
      }
      String query = SimpleSqlBuilder.substitute(
          "INSERT INTO ${table} (${columns}) VALUES (${values})",
          "table", getDBTable(),
          "columns", columns,
          "values", values);
      Object id = _queryExecutor.insert(_type.getName() + ".update()", query, parameters);
      if (id != null) {
        _columns.put(DBTypeInfo.ID_COLUMN, id);
        _new = false;
      }
    } else {
      StringBuilder values = new StringBuilder();
      List<IPreparedStatementParameter> params = new ArrayList<IPreparedStatementParameter>();
      for (int i = 0; i < columnValues.size(); i++) {
        if (i > 0) {
          values.append(", ");
        }
        values.append(SimpleSqlBuilder.substitute("${column} = ?", "column", columnValues.get(i)._column));
        params.add(columnValues.get(i)._parameter);
      }
      IDBColumn idColumn = getDBTable().getColumn(DBTypeInfo.ID_COLUMN);
      params.add(idColumn.wrapParameterValue(getId()));
      String query = SimpleSqlBuilder.substitute(
          "UPDATE ${table} SET ${values} WHERE ${idColumn} = ?",
          "table", getDBTable(),
          "values", values.toString(),
          "idColumn", idColumn
      );
      _queryExecutor.update(_type.getName() + ".update()", query, params.toArray(new IPreparedStatementParameter[params.size()]));
    }
  }

  private List<ColumnValuePair> gatherChangedValues() {
    // TODO - AHK - Actually compare to some stored-off map of the original values
    List<ColumnValuePair> columnValues = new ArrayList<ColumnValuePair>();
    // Note:  We iterate over the columns, in order, so that the query is always the same for a given set
    // of columns.  Iterating over the map keys might be more efficient, but could lead to different
    // orderings within the query, which would be less optimal on the database side
    for (IDBColumn column : getDBTable().getColumns()) {
      if (_columns.containsKey(column.getName())) {
        columnValues.add(new ColumnValuePair(column, column.wrapParameterValue(_columns.get(column.getName()))));
      }
    }
    return columnValues;
  }

  private static class ColumnValuePair {
    private IDBColumn _column;
    private IPreparedStatementParameter _parameter;

    private ColumnValuePair(IDBColumn column, IPreparedStatementParameter parameter) {
      _column = column;
      _parameter = parameter;
    }
  }

  @Override
  public void delete() throws SQLException {
    // TODO - AHK - Determine if we need to quote the table name or column names or not
    // TODO - AHK - What do we do if the table doesn't have an id?
    IDBColumn idColumn = getDBTable().getColumn(DBTypeInfo.ID_COLUMN);
    String query = SimpleSqlBuilder.substitute(
        "DELETE FROM ${table} WHERE ${idColumn} = ?",
        "table", getDBTable(),
        "idColumn", idColumn);
    _queryExecutor.delete(_type.getName() + ".delete()", query, idColumn.wrapParameterValue(getId()));
  }

  @Override
  public String toString() {
    return _columns.toString();
  }

  @Override
  public int hashCode() {
    int hashCode = _type.hashCode();
    List<String> keys = new ArrayList<String>(_columns.keySet());
    Collections.sort(keys);
    for (String columnName : keys) {
      if (_columns.get(columnName) != null) {
        hashCode = hashCode * 17 + _columns.get(columnName).hashCode();
      } else {
        hashCode *= 17;
      }
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CachedDBObject) {
      CachedDBObject other = (CachedDBObject) obj;
      if (_type.equals(other._type)) {
        for (String columnName : _columns.keySet()) {
          if (_columns.get(columnName) != null) {
            if (!_columns.get(columnName).equals(other._columns.get(columnName))) {
              return false;
            }
          }
        }
        for (String columnName : other._columns.keySet()) {
          if (other._columns.get(columnName) != null) {
            if (!other._columns.get(columnName).equals(_columns.get(columnName))) {
              return false;
            }
          }
        }
        return true;
      }
    }
    return false;
  }

  private IDBObject loadEntity(IDBTable table, Long id) {
    IDBColumn idColumn = table.getColumn(DBTypeInfo.ID_COLUMN);
    // TODO - AHK - Need some better way to convert between the two
    IDBType resultType = (IDBType) TypeSystem.getByFullName(table.getDatabase().getNamespace() + "." + table.getName());
    String sql = SimpleSqlBuilder.substitute("SELECT * FROM ${table} WHERE ${idColumn} = ?",
        "table", table,
        "idColumn", idColumn);
    IPreparedStatementParameter param = idColumn.wrapParameterValue(id);
    // TODO - AHK - Fetch this from somewhere?
    List<IDBObject> results = new QueryExecutorImpl(table.getDatabase()).selectEntity("CachedDBObject.loadEntity()", resultType, sql, param);
    if (results.isEmpty()) {
      return null;
    } else if (results.size() == 1) {
      return results.get(0);
    } else {
      throw new IllegalStateException("Expected to get one result back from query " + sql + " (" + param + ") but got " + results.size() );
    }
  }

}
