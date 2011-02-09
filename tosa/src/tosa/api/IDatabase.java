package tosa.api;

import tosa.DBConnection;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/9/11
 * Time: 4:30 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDatabase {

  // TODO - AHK - DB type, version, connection, etc.

  // TODO - AHK - This should be an interface to something in the API Package
  DBConnection getConnection();

  IDBTable getTable(String tableName);

  // TODO - AHK - Should this be an Iterable instead?
  Collection<? extends IDBTable> getAllTables();

  String getNamespace();

  // Query Execution Statements

  // TODO - AHK - Decide if this is really a good API

  IPreparedStatementParameter wrapParameter(Object value, IDBColumn column);

  // TODO - AHK - This should probably return more than just one object
  Object executeInsert(String sql, IPreparedStatementParameter... arguments);

  <T> List<T> executeSelect(String sql, IQueryResultProcessor<T> resultProcessor, IPreparedStatementParameter... arguments);

  List<Object> executeUpdate(String sql, IPreparedStatementParameter... arguments);

  void executeDelete(String sql, IPreparedStatementParameter... arguments);

  // TODO - AHK - Get CREATE TABLE statements
}
