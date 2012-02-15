package tosa.impl

uses tosa.api.IDatabase
uses tosa.api.IPreparedStatementParameter
uses tosa.loader.IDBType
uses tosa.api.IDBObject

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/15/12
 * Time: 8:49 AM
 * To change this template use File | Settings | File Templates.
 */
class QueryExecutorSpy implements QueryExecutor {
  private var _delegate : QueryExecutor
  private var _count : String
  private var _select : String
  private var _update : String
  private var _insert : String
  private var _delete : String

  construct(db : IDatabase) {
    _delegate = new QueryExecutorImpl(db);
  }

  override function count(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) : int {
    _count = sqlStatement;
    return _delegate.count(profilerTag, sqlStatement, parameters);
  }

  override function selectEntity(profilerTag : String, targetType : IDBType, sqlStatement : String, parameters : IPreparedStatementParameter[]) : List<IDBObject> {
    _select = sqlStatement;
    return _delegate.selectEntity(profilerTag, targetType, sqlStatement, parameters);
  }

  override function update(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) {
    _update = sqlStatement;
    _delegate.update(profilerTag, sqlStatement, parameters);
  }

  override function insert(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) : Object {
    _insert = sqlStatement;
    return _delegate.insert(profilerTag, sqlStatement, parameters)
  }

  override function delete(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) {
    _delete = sqlStatement;
    _delegate.delete(profilerTag, sqlStatement, parameters);
  }

  function countCalled() : boolean {
    return _count != null;
  }

  function selectCalled() : boolean {
    return _select != null;
  }

  function updateCalled() : boolean {
    return _update != null;
  }

  function insertCalled() : boolean {
    return _insert != null;
  }

  function deleteCalled() : boolean {
    return _delete != null;
  }

  function anyCalled() : boolean {
    return countCalled() || selectCalled() || updateCalled() || insertCalled() || deleteCalled();
  }

  function reset() {
    _count = null;
    _select = null;
    _update = null;
    _insert = null;
    _delete = null;
  }
}