package tosa.loader

uses tosa.loader.IDBType
uses tosa.api.IDBObject
uses gw.lang.reflect.features.PropertyReference
uses tosa.impl.util.StringSubstituter
uses tosa.impl.query.SqlStringSubstituter
uses java.util.Map
uses gw.util.Pair
uses tosa.api.QueryResult
uses java.lang.IllegalArgumentException


/**
 * This class effectively serves as a static mix-in for the DBType types.  Each function in here
 * needs to have its first argument be an IDBType object.  In addition, parameters that are
 * typed as IDBObject will be transformed at delegation time to the type in question, i.e.
 * something like tosa.testdb.Foo will be used in place of IDBObject, providing a sort of
 * covariance on the delegated type.  Any getter/setter pairs here will be turned into
 * properties for delegation purposes.  Every function on this class needs to be static.
 *
 * These methods SHOULD NOT be called directly by code:  they're purely here for delegation
 * purposes, to make it easier (and clearer) to write the static methods that are automatically
 * added to every DBType.
 */
class DBTypeDelegate {

  /**
   * Loads the entity with the given id.  Returns null if no entity exists with that id.
   *
   * @param dbType the implicit dbType initial argument
   * @param id the id to load
   */
  static function fromId(dbType : IDBType, id : long) : IDBObject {
    var table = dbType.Table
    // TODO - AHK - Should this be a constant?  Or a getIdColumn method since I call it so often?
    var idColumn = table.getColumn("id")
    var query = sub("SELECT * FROM :table WHERE :id_column = :id",
                    {"table" -> table, "id_column" -> idColumn, "id" -> id})

    var results = dbType.NewQueryExecutor.selectEntity(dbType.Name + ".fromId()", dbType, query.Sql, query.Params)

    if (results.Count == 0) {
      return null
    } else if (results.Count == 1) {
      return results.get(0)
    } else {
      throw "More than one row in table ${table.Name} had id ${id}";
    }
  }

  static function count(dbType : IDBType, sql : String, params : Map<String, Object> = null) : long {
    // TODO - AHK - Is this the right thing to enforce?  Should we enforce SELECT count(*) as count FROM <table>?
    // Should we even bother enforcing it here, or let it be enforced in NewQueryExecutor?
    if (!sql.startsWith("SELECT count(*) as count")) {
      throw new IllegalArgumentException("The count(String, Map) method must always be called with 'SELECT count(*) as count FROM' as the start of the statement.  The sql passed in was " + sql)
    }

    var countArgs = subIfNecessary(sql, null, params)
    return dbType.NewQueryExecutor.count(dbType.Name + ".count(String, Map)", countArgs.Sql, countArgs.Params)
  }

  static function countAll(dbType : IDBType) : long {
    var sql = sub("SELECT count(*) as count FROM :table", {"table" -> dbType.Table}).Sql
    return dbType.NewQueryExecutor.count(dbType.Name + ".countAll()", sql, {});
  }

  static function countWhere(dbType : IDBType, sql : String, params : Map<String, Object> = null) : long {
    // TODO - AHK - Is this the right thing to enforce?
    if (sql != null && sql.toUpperCase().startsWith("SELECT")) {
      throw new IllegalArgumentException("The countWhere(String, Map) method should only be called with the WHERE clause of a query.  To specify the full SQL for the query, use the count(String, Map) method instead.")
    }

    var queryPrefix = sub("SELECT count(*) as count FROM :table", {"table" -> dbType.Table}).Sql
    var countArgs = subIfNecessary(sql, queryPrefix, params)
    return dbType.NewQueryExecutor.count(dbType.Name + ".countWhere(String, Map)", countArgs.Sql, countArgs.Params)
  }

  static function countLike(dbType : IDBType, template: IDBObject) : long {
    var queryPrefix = sub("SELECT count(*) as count FROM :table", {"table" -> dbType.Table}).Sql
    var whereClause = buildWhereClause(template)
    return dbType.NewQueryExecutor.count(dbType.Name + ".countLike(" + dbType.Name + ")", queryPrefix + whereClause.Sql, whereClause.Params)
  }

  static function select(dbType : IDBType, sql : String, params : Map<String, Object> = null) : QueryResult<IDBObject> {
    // TODO - AHK - Is this the right thing to enforce?  Should we enforce SELECT * FROM <table>?
    // Should we even bother enforcing it here, or let it be enforced in NewQueryExecutor?
    if (!sql.toUpperCase().startsWith("SELECT * FROM")) {
      throw new IllegalArgumentException("The select(String, Map) method must always be called with 'SELECT * FROM' as the start of the statement.  The sql passed in was " + sql)
    }

    var selectArgs = subIfNecessary(sql, null, params)
    return dbType.NewQueryExecutor.selectEntity(dbType.Name + ".select(String, Map)", dbType, selectArgs.Sql, selectArgs.Params)
  }

  static function selectAll(dbType : IDBType) : QueryResult<IDBObject> {
    var sql = sub("SELECT * FROM :table", {"table" -> dbType.Table}).Sql
    return dbType.NewQueryExecutor.selectEntity(dbType.Name + ".selectAll()", dbType, sql, {})
  }

  static function selectWhere(dbType : IDBType, sql : String, params : Map<String, Object> = null) : QueryResult<IDBObject> {
    // TODO - AHK - Is this the right thing to enforce?
    if (sql != null && sql.toUpperCase().startsWith("SELECT")) {
      throw new IllegalArgumentException("The selectWhere(String, Map) method should only be called with the WHERE clause of a query.  To specify the full SQL for the query, use the select(String, Map) method instead.")
    }

    var queryPrefix = sub("SELECT * FROM :table", {"table" -> dbType.Table}).Sql
    var selectArgs = subIfNecessary(sql, queryPrefix, params)
    return dbType.NewQueryExecutor.selectEntity(dbType.Name + ".selectWhere(String, Map)", dbType, selectArgs.Sql, selectArgs.Params)
  }

  static function selectLike(dbType : IDBType, template : IDBObject) : QueryResult<IDBObject> {
    var queryPrefix = sub("SELECT * FROM :table", {"table" -> dbType.Table}).Sql
    var whereClause = buildWhereClause(template)
    return dbType.NewQueryExecutor.selectEntity(dbType.Name + ".selectWhere(String, Map)", dbType, queryPrefix + whereClause.Sql, whereClause.Params)
  }

  private static function subIfNecessary(sql : String, prefix : String, params : Map<String, Object>) : SqlAndParams {
    var queryString : String
    var paramArray : Object[]
    if (sql == null || sql.Empty) {
      queryString = prefix
      paramArray = {}
    } else if (params != null) {
      var query = sub(sql, params)
      queryString = (prefix == null ? query.Sql : prefix + " WHERE " + query.Sql)
      paramArray = query.Params
    } else {
      queryString = (prefix == null ? sql : prefix + " WHERE " + sql)
      paramArray = {}
    }
    return new SqlAndParams(queryString, paramArray)
  }

  private static function sub(input : String, tokenValues : Map<String, Object>) : SqlAndParams {
    var pair = SqlStringSubstituter.substitute(input, tokenValues)
    return new SqlAndParams(pair.First, pair.Second)
  }

  private static function buildWhereClause(template : IDBObject) : SqlAndParams {
    var clauses : List<String> = {}
    var params : List<Object> = {}
    if (template != null) {
      for (column in template.DBTable.Columns) {
        var value = template.getColumnValue(column.Name)
        if (value != null) {
          var result = sub(":column = :value", {"column" -> column, "value" -> value})
          clauses.add(result.Sql)
          params.add(result.Params[0])
        }
      }
    }

    if (not clauses.Empty) {
      return new SqlAndParams(" WHERE " + clauses.join(" AND "), params.toTypedArray())
    } else {
      // TODO - AHK - Should we just leave the clause out entirely?
      return new SqlAndParams(" WHERE 1 = 1", params.toTypedArray())
    }
  }

  static function findSorted(dbType : IDBType, template : IDBObject, sortProperty : PropertyReference<IDBObject, Object>, ascending : boolean) : List<IDBObject> {
    return dbType.Finder.findSorted(template, sortProperty, ascending)
  }

  static function findPaged(dbType : IDBType, template : IDBObject, pageSize : int, offset : int) : List<IDBObject> {
    return dbType.Finder.findPaged(template, pageSize, offset)
  }

  static function findSortedPaged(dbType : IDBType, template : IDBObject, sortProperty : PropertyReference<IDBObject, Object>, ascending : boolean, pageSize : int, offset : int) : List<IDBObject> {
    return dbType.Finder.findSortedPaged(template, sortProperty, ascending, pageSize, offset)
  }

  private static class SqlAndParams {
    private var _sql : String as Sql
    private var _params : Object[] as Params

    construct(sqlArg : String, paramsArg : Object[]) {
      _sql = sqlArg
      _params = paramsArg
    }
  }
}