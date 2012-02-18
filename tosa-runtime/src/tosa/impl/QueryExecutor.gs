package tosa.impl

uses tosa.api.IPreparedStatementParameter
uses tosa.loader.IDBType
uses tosa.api.IDBObject

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/16/12
 * Time: 12:03 AM
 * To change this template use File | Settings | File Templates.
 */
interface QueryExecutor {
  function count(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) : int

  function selectEntity(profilerTag : String, targetType : IDBType, sqlStatement : String, parameters : IPreparedStatementParameter[]) : List<IDBObject>

  function update(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[])

  function insert(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[]) : Object

  function delete(profilerTag : String, sqlStatement : String, parameters : IPreparedStatementParameter[])
}