package tosa.impl

uses tosa.api.IDBObject
uses java.util.Map
uses tosa.api.EntityCollection
uses tosa.loader.IDBType
uses gw.lang.reflect.TypeSystem
uses java.util.HashMap
uses tosa.api.IDBTable
uses java.lang.Long
uses java.lang.IllegalStateException
uses java.lang.IllegalArgumentException
uses tosa.api.IDBColumn
uses tosa.loader.DBTypeInfo
uses tosa.api.IDBArray
uses tosa.api.IDBFkArray
uses tosa.api.IDBJoinArray
uses tosa.api.IPreparedStatementParameter
uses java.util.Collections
uses java.util.ArrayList
uses java.lang.StringBuilder

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/9/12
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
class CachedDBObjectDelegateImpl implements CachedDBObject.Delegate {
  private var _columns : Map<String, Object>;
  private var _cachedFks : Map<String, IDBObject>;
  private var _cachedArrays : Map<String, EntityCollection>;
  private var _queryExecutor : QueryExecutor;
  private var _type : IDBType;
  private var _new : boolean;
  private var _owner : CachedDBObject

  construct(type : IDBType, isNew : boolean, owner : CachedDBObject) {
    _type = type
    _new = isNew
    _owner = owner
    // TODO - AHK
    _columns = new HashMap<String, Object>();
    _cachedFks = new HashMap<String, IDBObject>();
    _cachedArrays = new HashMap<String, EntityCollection>();
    _queryExecutor = new QueryExecutorImpl(_type.getTable().getDatabase());
    // TODO - AHK - There's room for perf improvements here
  }

  private property get TableName() : String {
    return DBTable.Name
  }

  override property get DBTable() : IDBTable {
    return _type.getTable();
  }

  override property get Id() : Long {
    return getColumnValue(DBTypeInfo.ID_COLUMN) as Long
  }

  override property get New() : boolean {
    return _new;
  }

  override function getColumnValue(columnName : String) : Object {
    // TODO - AHK - Validate that the column name is actually a legal column   
    return _columns.get(columnName)
  }

  override function setColumnValue(columnName : String, value : Object) {
    // TODO - AHK - Validate that the column name is legal and that the value is legal
    // TODO - AHK - Invalidate any fk back-pointers associated with that column if the value has changed
    _columns.put(columnName, value)  
  }

  override function getFkValue(columnName : String ) : IDBObject {
    var column = getAndValidateFkColumn(columnName)
  
    var fkObject = _cachedFks.get(columnName)
    if (fkObject != null) {
      return fkObject;
    }
  
    var fkID = _columns.get(columnName) as Long
    if (fkID == null) {
      return null;
    }
  
    fkObject = loadEntity(column.getFKTarget(), fkID);
    if (fkObject == null) {
      throw new IllegalStateException("Column " + columnName + " on table " + _type.getTable().getName() + " has a value of " + fkID + ", but no corresponding row was found in the database");
    }
  
    return fkObject;  
  }
  
  override function setFkValue(columnName : String, value : IDBObject) {
    var column = getAndValidateFkColumn(columnName);
    // TODO - AHK - Validate that the value is of the correct type
    if (value == null) {
      _columns.put(columnName, null);
      _cachedFks.put(columnName, null);
    } else {
      _columns.put(columnName, value.getId());
      _cachedFks.put(columnName, value);
    }
  }

  private function getAndValidateFkColumn(columnName : String) : IDBColumn {
    var column = _type.getTable().getColumn(columnName);
    if (column == null) {
      throw new IllegalArgumentException("Column name " + columnName + " is not a valid column on the  " + _type.getTable().getName() + " table");
    }
    if (!column.isFK()) {
      throw new IllegalArgumentException("Column " + columnName + " on table " + _type.getTable().getName() + " is not a foreign key");
    }
    return column;
  }
  
  override function getArray(arrayName : String) : EntityCollection {
    return getArray(_type.getTable().getArray(arrayName));  
  }

  override function getArray(dbArray : IDBArray) : EntityCollection {
    var result = _cachedArrays.get(dbArray.getPropertyName());
    if (result == null) {
      if (dbArray typeis IDBFkArray) {
        var fkColumn = dbArray.getFkColumn();
        var fkType = TypeSystem.getByFullName(fkColumn.getTable().getDatabase().getNamespace() + "." + fkColumn.getTable().getName()) as IDBType
        result = new ReverseFkEntityCollectionImpl(_owner, fkType, fkColumn, new QueryExecutorImpl(fkColumn.getTable().getDatabase()));
      } else if (dbArray typeis IDBJoinArray) {
        var targetType = TypeSystem.getByFullName(getDBTable().getDatabase().getNamespace() + "." + dbArray.getTargetTable().getName()) as IDBType
        result = new JoinArrayEntityCollectionImpl(_owner, targetType, dbArray.getSrcColumn(), dbArray.getTargetColumn(), new QueryExecutorImpl(getDBTable().getDatabase()));
      }
      _cachedArrays.put(dbArray.getPropertyName(), result);
    }
    return result;
  }

  override function toID() : Long {
    return Id
  }

  // TODO - AHK - Kill this
  property get Columns() : Map<String, Object> {
    return _columns
  }

  override function update() {
    var columnValues = gatherChangedValues();
    if (_new) {
      var columns = new ArrayList<IDBColumn>();
      var values = new ArrayList<String>();
      var parameters = new IPreparedStatementParameter[columnValues.size()];
      for (pair in columnValues index i) {
        columns.add(pair._column);
        values.add("?");
        parameters[i] = pair._parameter;
      }
      var query = SimpleSqlBuilder.substitute(
          "INSERT INTO \${table} (\${columns}) VALUES (\${values})",
          "table", getDBTable(),
          "columns", columns,
          "values", values);
      var id = _queryExecutor.insert(_type.getName() + ".update()", query, parameters);
      if (id != null) {
        _columns.put(DBTypeInfo.ID_COLUMN, id);
        _new = false;
      }
    } else {
      var values = new StringBuilder();
      var params = new ArrayList<IPreparedStatementParameter>();
      for (i in 0..|columnValues.size()) {
        if (i > 0) {
          values.append(", ");
        }
        values.append(SimpleSqlBuilder.substitute("\${column} = ?", "column", columnValues.get(i)._column));
        params.add(columnValues.get(i)._parameter);
      }
      var idColumn = getDBTable().getColumn(DBTypeInfo.ID_COLUMN);
      params.add(idColumn.wrapParameterValue(getId()));
      var query = SimpleSqlBuilder.substitute(
          "UPDATE \${table} SET \${values} WHERE \${idColumn} = ?",
          "table", getDBTable(),
          "values", values.toString(),
          "idColumn", idColumn
      );
      _queryExecutor.update(_type.getName() + ".update()", query, params.toArray(new IPreparedStatementParameter[params.size()]));
    }
  }

  private function gatherChangedValues(): List <ColumnValuePair> {
    // TODO - AHK - Actually compare to some stored-off map of the original values
    var columnValues = new ArrayList <ColumnValuePair>();
    // Note:  We iterate over the columns, in order, so that the query is always the same for a given set
    // of columns.  Iterating over the map keys might be more efficient, but could lead to different
    // orderings within the query, which would be less optimal on the database side
    for (column in getDBTable().getColumns()) {
      if (_columns.containsKey(column.getName())) {
        columnValues.add(new ColumnValuePair(column, column.wrapParameterValue(_columns.get(column.getName()))));
      }
    }
    return columnValues;
  }

  private static class ColumnValuePair {
    private var _column : IDBColumn;
    private var _parameter : IPreparedStatementParameter;

    private construct(column : IDBColumn, parameter : IPreparedStatementParameter) {
      _column = column;
      _parameter = parameter;
    }
  }

  override function delete() {
    // TODO - AHK - Determine if we need to quote the table name or column names or not
    // TODO - AHK - What do we do if the table doesn't have an id?
    var idColumn = getDBTable().getColumn(DBTypeInfo.ID_COLUMN);
    var query = SimpleSqlBuilder.substitute(
        "DELETE FROM \${table} WHERE \${idColumn} = ?",
        "table", getDBTable(),
        "idColumn", idColumn);
    _queryExecutor.delete(_type.getName() + ".delete()", query, {idColumn.wrapParameterValue(getId())});
  }

  override function toString() : String {
    return _columns.toString();
  }

  override function hashCode() : int {
    var hashCode = _type.hashCode();
    var keys = new ArrayList<String>(_columns.keySet());
    Collections.sort(keys);
    for (columnName in keys) {
      if (_columns.get(columnName) != null) {
        hashCode = hashCode * 17 + _columns.get(columnName).hashCode();
      } else {
        hashCode *= 17;
      }
    }
    return hashCode;
  }

  override function equals(obj: Object): boolean {
    if (obj === this) {
      return true
    }

    if (obj typeis CachedDBObjectDelegateImpl) {
      if (_type.equals(obj._type)) {
        for (columnName in _columns.keySet()) {
          if (_columns.get(columnName) != null) {
            if (!_columns.get(columnName).equals(obj._columns.get(columnName))) {
              return false;
            }
          }
        }
        for (columnName in obj._columns.keySet()) {
          if (obj._columns.get(columnName) != null) {
            if (!obj._columns.get(columnName).equals(_columns.get(columnName))) {
              return false;
            }
          }
        }
        return true;
      }
    }
    return false;
  }

  private function loadEntity(table : IDBTable, id : Long) : IDBObject {
    var idColumn = table.getColumn(DBTypeInfo.ID_COLUMN);
    // TODO - AHK - Need some better way to convert between the two
    var resultType = TypeSystem.getByFullName(table.getDatabase().getNamespace() + "." + table.getName()) as IDBType
    var sql = SimpleSqlBuilder.substitute("SELECT * FROM \${table} WHERE \${idColumn} = ?",
                                          "table", table,
                                          "idColumn", idColumn);
    var param = idColumn.wrapParameterValue(id);
    // TODO - AHK - Fetch this from somewhere?
    var results = new QueryExecutorImpl(table.getDatabase()).selectEntity("CachedDBObject.loadEntity()", resultType, sql, {param});
    if (results.isEmpty()) {
      return null;
    } else if (results.size() == 1) {
      return results.get(0);
    } else {
      throw new IllegalStateException("Expected to get one result back from query " + sql + " (" + param + ") but got " + results.size() );
    }
  }

}