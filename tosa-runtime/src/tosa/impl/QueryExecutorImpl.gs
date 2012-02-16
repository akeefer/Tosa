package tosa.impl

uses tosa.api.IDatabase
uses tosa.api.IPreparedStatementParameter
uses tosa.loader.Util
uses java.util.Arrays
uses java.lang.IllegalStateException
uses tosa.api.IDBObject
uses tosa.loader.IDBType
uses java.sql.ResultSet

/**
 *
 */
class QueryExecutorImpl implements QueryExecutor {

  private var _db : IDatabase

  public construct(db : IDatabase) {
    _db = db;
  }

  // TODO - AHK - Clean up the query execution API for reals . . .

  // TODO - AHK - Should this be a long instead?
  override function count(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) : int {
    // TODO - AHK - Verify that it starts with "SELECT count(*) as count"
    var profiler = Util.newProfiler(profilerTag);
    profiler.start(sqlStatement + " (" + Arrays.asList(parameters) + ")");
    try {
      var results = _db.getDBExecutionKernel().executeSelect(
          sqlStatement,
          \r -> r.getInt("count"),
          parameters);
      if (results.size() == 0) {
        return 0;
      } else if (results.size() == 1) {
        return results.get(0);
      } else {
        throw new IllegalStateException("Expected count query " + sqlStatement + " to return 0 or 1 result, but got " + results.size());
      }
    } finally {
      profiler.stop();
    }
  }

  override function selectEntity(profilerTag : String, type : IDBType, sqlStatement : String, parameters : IPreparedStatementParameter[]) : List<IDBObject> {
    // TODO - AHK - Ensure that it starts with SELECT * ?
    // TODO - AHK - Verify that the db type is from the database we have?
    var profiler = Util.newProfiler(profilerTag);
    profiler.start(sqlStatement + " (" + Arrays.asList(parameters) + ")");
    try {
      return _db.getDBExecutionKernel().executeSelect(sqlStatement,
          \r -> buildObject(type, r),
          parameters);
    } finally {
      profiler.stop();
    }
  }

  override function update(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) {
    // TODO - AHK - Verify it starts with UPDATE ?
    var profiler = Util.newProfiler(profilerTag);
    profiler.start(sqlStatement + " (" + Arrays.asList(parameters) + ")");
    try {
      _db.getDBExecutionKernel().executeUpdate(sqlStatement,
          parameters);
    } finally {
      profiler.stop();
    }
  }

  override function insert(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) : Object {
    // TODO - AHK - Verify it starts with INSERT ?
    var profiler = Util.newProfiler(profilerTag);
    profiler.start(sqlStatement + " (" + Arrays.asList(parameters) + ")");
    try {
      return _db.getDBExecutionKernel().executeInsert(sqlStatement,
          parameters);
    } finally {
      profiler.stop();
    }
  }

 override function delete(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) {
    // TODO - AHK - Verify it starts with DELETE ?
    var profiler = Util.newProfiler(profilerTag);
    profiler.start(sqlStatement + " (" + Arrays.asList(parameters) + ")");
    try {
      _db.getDBExecutionKernel().executeDelete(sqlStatement,
          parameters);
    } finally {
      profiler.stop();
    }
  }

  // TODO - AHK - Find a home for this function.  Maybe on CachedDBObject?  Or somewhere else?

  static function buildObject(type : IDBType, resultSet : ResultSet) : IDBObject {
    var obj = new CachedDBObject(type, false)
    var table = type.getTable();
    for (column in table.getColumns()) {
      // TODO - AHK - This is a little sketch, perhaps
      var resultObject = column.getColumnType().readFromResultSet(resultSet, table.getName() + "." + column.getName());
      obj.setColumnValue(column.getName(), resultObject);
    }
    return obj;
  }

}