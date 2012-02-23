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
uses tosa.impl.query.SqlStringSubstituter
uses java.lang.System

/**
 * This is the real implementation of the IDBObject interface.  The CachedDBObject itself is just
 * a wrapper that calls through to this class.
 */
class CachedDBObjectDelegateImpl implements CachedDBObject.Delegate {

  // The column values associated with this object
  private var _tempColumns : ColumnMap

  // Cached IDBObjects for fks on this object.  Note that the _columns
  // map also reflects these values, but that it's possible for a value
  // to be in _columns but not be loaded into _cachedFks yet
  private var _cachedFks : Map<String, IDBObject>

  // Cached EntityCollection objects for any "arrays" hanging off
  // of this object
  private var _cachedArrays : Map<String, EntityCollection>

  // The QueryExecutor used to issue any update/insert/delete
  // queries, and that's passed down to any EntityCollections
  private var _queryExecutor : QueryExecutor

  // The IDBType for this entity
  private var _type : IDBType

  // The actual IDBObject that is delegating to this class.  We need it so
  // we can pass it through to any EntityCollections we construct
  private var _owner : CachedDBObject

  construct(type : IDBType, originalColumnValues : Map<String, Object>, owner : CachedDBObject) {
    _type = type
    _tempColumns = new ColumnMap(originalColumnValues)
    _owner = owner
    _cachedFks = new HashMap<String, IDBObject>()
    _cachedArrays = new HashMap<String, EntityCollection>()
    _queryExecutor = new QueryExecutorImpl(_type.Table.Database)
  }

  private property get TableName() : String {
    return DBTable.Name
  }

  override property get DBTable() : IDBTable {
    return _type.Table
  }

  override property get Id() : Long {
    return getColumnValue(DBTypeInfo.ID_COLUMN) as Long
  }

  override property get New() : boolean {
    return !_tempColumns.HasOriginalValues
  }

  override function getColumnValue(columnName : String) : Object {
    // TODO - AHK - Validate that the column name is actually a legal column
    return _tempColumns.get(columnName)
  }

  override function setColumnValue(columnName : String, value : Object) {
    // TODO - AHK - Validate that the column name is legal and that the value is legal
    // TODO - AHK - Invalidate any fk back-pointers associated with that column if the value has changed
    _tempColumns.put(columnName, value)
  }

  override function getFkValue(columnName : String ) : IDBObject {
    var column = getAndValidateFkColumn(columnName)
  
    var fkObject = _cachedFks.get(columnName)
    if (fkObject != null) {
      return fkObject
    }
  
    var fkID = _tempColumns.get(columnName) as Long
    if (fkID == null) {
      return null
    }
  
    fkObject = loadEntity(column.FKTarget, fkID)
    if (fkObject == null) {
      throw new IllegalStateException("Column " + columnName + " on table " + _type.Table.Name + " has a value of " + fkID + ", but no corresponding row was found in the database")
    }
  
    return fkObject  
  }
  
  override function setFkValue(columnName : String, value : IDBObject) {
    var column = getAndValidateFkColumn(columnName)
    // TODO - AHK - Validate that the value is of the correct type
    if (value == null) {
      // TODO - AHK - Do something around removing the key entirely if it matches the original value
      _tempColumns.put(columnName, null)
      _cachedFks.put(columnName, null)
    } else {
      _tempColumns.put(columnName, value.Id)
      _cachedFks.put(columnName, value)
    }
  }

  private function getAndValidateFkColumn(columnName : String) : IDBColumn {
    var column = _type.Table.getColumn(columnName)
    if (column == null) {
      throw new IllegalArgumentException("Column name " + columnName + " is not a valid column on the  " + _type.Table.Name + " table")
    }
    if (!column.isFK()) {
      throw new IllegalArgumentException("Column " + columnName + " on table " + _type.Table.Name + " is not a foreign key")
    }
    return column
  }
  
  override function getArray(arrayName : String) : EntityCollection {
    return getArray(_type.Table.getArray(arrayName))
  }

  override function getArray(dbArray : IDBArray) : EntityCollection {
    var result = _cachedArrays.get(dbArray.PropertyName)
    if (result == null) {
      if (dbArray typeis IDBFkArray) {
        var fkColumn = dbArray.FkColumn
        var fkType = TypeSystem.getByFullName(fkColumn.Table.Database.Namespace + "." + fkColumn.Table.Name) as IDBType
        result = new ReverseFkEntityCollectionImpl(_owner, fkType, fkColumn, _queryExecutor)
      } else if (dbArray typeis IDBJoinArray) {
        var targetType = TypeSystem.getByFullName(DBTable.Database.Namespace + "." + dbArray.TargetTable.Name) as IDBType
        result = new JoinArrayEntityCollectionImpl(_owner, targetType, dbArray.SrcColumn, dbArray.TargetColumn, _queryExecutor)
      }
      _cachedArrays.put(dbArray.PropertyName, result)
    }
    return result
  }

  override function toID() : Long {
    return Id
  }

  override function update() {
    var columnValues = gatherChangedValues()
    if (New) {
      var columns = new ArrayList<IDBColumn>()
      var values = new ArrayList<String>()
      var parameters = new IPreparedStatementParameter[columnValues.size()]
      for (pair in columnValues index i) {
        columns.add(pair._column)
        values.add("?")
        parameters[i] = pair._parameter
      }
      var query = "INSERT INTO " + DBTable.PossiblyQuotedName + " (" + columns.map(\c -> c.PossiblyQuotedName).join(", ") + ") VALUES (" + values.join(", ") + ")"
      var id = _queryExecutor.insert(_type.Name + ".update()", query, parameters)
      if (id != null) {
        _tempColumns.put(DBTypeInfo.ID_COLUMN, id)
      }
      _tempColumns.acceptChanges()
    } else if (!columnValues.Empty) {
      var values = new StringBuilder()
      var params = new ArrayList<IPreparedStatementParameter>()
      for (i in 0..|columnValues.size()) {
        if (i > 0) {
          values.append(", ")
        }
        values.append(columnValues.get(i)._column.PossiblyQuotedName).append(" = ?")
        params.add(columnValues.get(i)._parameter)
      }
      var idColumn = DBTable.getColumn(DBTypeInfo.ID_COLUMN)
      params.add(idColumn.wrapParameterValue(Id))
      var query = "UPDATE " + DBTable.PossiblyQuotedName + " SET " + values + " WHERE " + idColumn.PossiblyQuotedName + " = ?"
      _queryExecutor.update(_type.Name + ".update()", query, params.toArray(new IPreparedStatementParameter[params.size()]))
      _tempColumns.acceptChanges()
    } else {
      // No-op:  nothing's changed
    }
  }

  private function gatherChangedValues(): List <ColumnValuePair> {
    // TODO - AHK - Actually compare to some stored-off map of the original values
    var columnValues = new ArrayList <ColumnValuePair>()
    // Note:  We iterate over the columns, in order, so that the query is always the same for a given set
    // of columns.  Iterating over the map keys might be more efficient, but could lead to different
    // orderings within the query, which would be less optimal on the database side
    for (column in DBTable.Columns) {
      if (_tempColumns.isValueChanged(column.Name)) {
        columnValues.add(new ColumnValuePair(column, column.wrapParameterValue(_tempColumns.get(column.Name))))
      }
    }
    return columnValues
  }

  private static class ColumnValuePair {
    private var _column : IDBColumn
    private var _parameter : IPreparedStatementParameter

    private construct(column : IDBColumn, parameter : IPreparedStatementParameter) {
      _column = column
      _parameter = parameter
    }
  }

  override function delete() {
    // TODO - AHK - Determine if we need to quote the table name or column names or not
    // TODO - AHK - What do we do if the table doesn't have an id?
    var idColumn = DBTable.getColumn(DBTypeInfo.ID_COLUMN)
    var query = SqlStringSubstituter.substitute("DELETE FROM :table WHERE :idColumn = :id",
        {"table" -> DBTable,
         "idColumn" -> idColumn,
         "id" -> Id})
    _queryExecutor.delete(_type.Name + ".delete()", query.Sql, query.Params)
  }

  override function toString() : String {
    return _tempColumns.toString()
  }

  override function hashCode() : int {
    var hashCode = _type.hashCode()
    if (Id != null) {
      hashCode *= Id.hashCode()  
    } else {
      hashCode *= System.identityHashCode(this)
    }
    
    return hashCode
  }

  override function equals(obj: Object): boolean {
    if (obj === this) {
      return true
    }

    if (Id != null && obj typeis CachedDBObjectDelegateImpl) {
      return Id == obj.Id  
    }
   
    return false
  }

  private function loadEntity(table : IDBTable, id : Long) : IDBObject {
    var idColumn = table.getColumn(DBTypeInfo.ID_COLUMN)
    // TODO - AHK - Need some better way to convert between the two
    var resultType = TypeSystem.getByFullName(table.Database.Namespace + "." + table.Name) as IDBType
    var query = SqlStringSubstituter.substitute("SELECT * FROM :table WHERE :idColumn = :id",
        {"table" -> table,
         "idColumn" -> idColumn,
         "id" -> id})
    // TODO - AHK - Fetch this from somewhere?
    var results = _queryExecutor.selectEntity("CachedDBObjectDelegateImpl.loadEntity()", resultType, query.Sql, query.Params)
    if (results.Empty) {
      return null
    } else if (results.size() == 1) {
      return results.get(0)
    } else {
      throw new IllegalStateException("Expected to get one result back from query " + query.Sql + " (" + query.Params + ") but got " + results.size() )
    }
  }

}