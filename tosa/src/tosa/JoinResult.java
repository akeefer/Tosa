package tosa;

import tosa.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoinResult implements List<CachedDBObject> {

  private List<CachedDBObject> _result;

  private IDatabase _database;
  private IDBTable _joinTable;
  private IDBColumn _srcColumn;
  private IDBColumn _targetColumn;
  private String _id;

  public JoinResult(List<CachedDBObject> result, IDatabase database, IDBTable joinTable,
                    IDBColumn srcColumn, IDBColumn targetColumn, String id) {
    _result = result;
    _database = database;
    _joinTable = joinTable;
    _srcColumn = srcColumn;
    _targetColumn = targetColumn;
    _id = id;
  }

  @Override
  public boolean add(CachedDBObject obj) {
    // TODO - AHK - Hardcode ID as a column name somewhere
    // TODO - AHK - Determine dynamically if table names should be quoted or not
    String query = "insert into \"" + _joinTable.getName() + "\" (\"" + _srcColumn.getName() + "\", \"" + _targetColumn.getName() + "\") values (?, ?)";
    IPreparedStatementParameter[] parameters = new IPreparedStatementParameter[2];
    parameters[0] = _database.wrapParameter(_id, _srcColumn);
    parameters[1] = _database.wrapParameter(obj.getColumns().get("id"), _targetColumn);
    _database.executeInsert(query, parameters);
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends CachedDBObject> objs) {
    StringBuilder query = new StringBuilder("insert into \"");
    query.append(_joinTable.getName()).append("\" (\"").append(_srcColumn.getName()).append("\", \"").append(_targetColumn.getName()).append("\") values ");
    for (CachedDBObject obj : objs) {
      query.append("(").append(_id).append(", ").append(obj.getColumns().get("id")).append(")");
      query.append(", ");
    }
    if (!objs.isEmpty()) {
      query.setLength(query.length() - 2);
    }
    try {
      Connection conn = _database.getConnection().connect();
      try {
        Statement stmt = conn.createStatement();
        try {
          stmt.executeUpdate(query.toString());
        } finally {
          stmt.close();
        }
      } finally {
        conn.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return true;
  }

  @Override
  public boolean remove(Object o) {
    // TODO - AHK - Ensure it's an object of the right type
    if (o instanceof CachedDBObject) {
      CachedDBObject obj = (CachedDBObject) o;
      if (_joinTable.hasId()) {
        // TODO - AHK - CRITICAL - Execute the select and delete in a single transaction
        String selectQuery = "select \"id\" from \"" + _joinTable.getName() + "\" where \"" + _srcColumn.getName() + "\" = ? and \"" + _targetColumn.getName() + "\" = ? limit 1";
        List<Long> ids =_database.executeSelect(selectQuery,
            new IQueryResultProcessor<Long>() {
              @Override
              public Long processResult(ResultSet result) throws SQLException {
                return result.getLong("id");
              }
            },
            _database.wrapParameter(_id, _srcColumn), _database.wrapParameter(obj.getColumns().get("id"), _targetColumn));
        if (ids.size() > 0) {
          // TODO -- Throw if there's more than 1
          String deleteStatement = "delete from \"" + _joinTable.getName() + "\" where \"id\" = ?";
          _database.executeDelete(deleteStatement, _database.wrapParameter(ids.get(0), _joinTable.getColumn("id")));
          return true;
        }
      } else {
        String query = "delete from \"" + _joinTable.getName() + "\" where \"" + _srcColumn.getName() + "\" = ? and \"" + _targetColumn.getName() + "\" = ?";
        IPreparedStatementParameter[] parameters = new IPreparedStatementParameter[2];
        parameters[0] = _database.wrapParameter(_id, _srcColumn);
        parameters[1] = _database.wrapParameter(obj.getColumns().get("id"), _targetColumn);
        _database.executeDelete(query, parameters);
      }
    }

    return false;
  }

  @Override
  public void clear() {
    String query = "delete from \"" + _joinTable.getName() + "\" where \"" + _srcColumn.getName() + "\" = ?";
    _database.executeDelete(query, _database.wrapParameter(_id, _srcColumn));
  }


  @Override
  public int size() {
    return _result.size();
  }

  @Override
  public boolean isEmpty() {
    return _result.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return _result.contains(o);
  }

  @Override
  public Iterator<CachedDBObject> iterator() {
    return _result.iterator();
  }

  @Override
  public Object[] toArray() {
    return _result.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return _result.toArray(a);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return _result.containsAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends CachedDBObject> c) {
    return _result.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return _result.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return _result.retainAll(c);
  }

  @Override
  public boolean equals(Object o) {
    return _result.equals(o);
  }

  @Override
  public int hashCode() {
    return _result.hashCode();
  }

  @Override
  public CachedDBObject get(int index) {
    return _result.get(index);
  }

  @Override
  public CachedDBObject set(int index, CachedDBObject element) {
    return _result.set(index, element);
  }

  @Override
  public void add(int index, CachedDBObject element) {
    _result.add(index, element);
  }

  @Override
  public CachedDBObject remove(int index) {
    return _result.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return _result.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return _result.lastIndexOf(o);
  }

  @Override
  public ListIterator<CachedDBObject> listIterator() {
    return _result.listIterator();
  }

  @Override
  public ListIterator<CachedDBObject> listIterator(int index) {
    return _result.listIterator(index);
  }

  @Override
  public List<CachedDBObject> subList(int fromIndex, int toIndex) {
    return _result.subList(fromIndex, toIndex);
  }

}
