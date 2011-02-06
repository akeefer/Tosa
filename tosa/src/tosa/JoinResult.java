package tosa;

import org.slf4j.profiler.Profiler;
import tosa.api.IDatabase;
import tosa.loader.DBTypeInfo;
import tosa.loader.Util;

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
  private String _joinTableName;
  private String _srcTableName;
  private String _targetTableName;
  private String _id;

  public JoinResult(List<CachedDBObject> result, IDatabase database, String joinTableName,
                    String srcTableName, String targetTableName, String id) {
    _result = result;
    _database = database;
    _joinTableName = joinTableName;
    _srcTableName = srcTableName;
    _targetTableName = targetTableName;
    _id = id;
  }

  @Override
  public boolean add(CachedDBObject obj) {
    String query = "insert into \"" + _joinTableName + "\" (\"" + _srcTableName + "_id\", \"" + _targetTableName + "_id\") values (" + _id + ", " + obj.getColumns().get(DBTypeInfo.ID_COLUMN) + ")";
    Profiler profiler = Util.newProfiler(_srcTableName + "." + _joinTableName + ".add()");
    profiler.start(query);
    try {
      Connection conn = _database.getConnection().connect();
      try {
        Statement stmt = conn.createStatement();
        try {
          stmt.executeUpdate(query);
        } finally {
          stmt.close();
        }
      } finally {
        conn.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      profiler.stop();
    }
    _result.add(obj);
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends CachedDBObject> objs) {
    StringBuilder query = new StringBuilder("insert into \"");
    query.append(_joinTableName).append("\" (\"").append(_srcTableName).append("_id\", \"").append(_targetTableName).append("_id\") values ");
    for (CachedDBObject obj : objs) {
      query.append("(").append(_id).append(", ").append(obj.getColumns().get(DBTypeInfo.ID_COLUMN)).append(")");
      query.append(", ");
    }
    if (!objs.isEmpty()) {
      query.setLength(query.length() - 2);
    }
    Profiler profiler = Util.newProfiler(_srcTableName + "." + _joinTableName + ".addAll()");
    profiler.start(query.toString());
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
    } finally {
      profiler.stop();
    }
    _result.addAll(objs);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    Profiler profiler = Util.newProfiler(_srcTableName + "." + _joinTableName + ".remove()");
    if (o instanceof CachedDBObject) {
      CachedDBObject obj = (CachedDBObject) o;
      try {
        Connection conn = _database.getConnection().connect();
        try {
          Statement stmt = conn.createStatement();
          try {
            if (_database.getTable(_joinTableName).hasId()) {
              String query = "select * from \"" + _joinTableName + "\" where \"" + _srcTableName + "_id\" = " + _id + " and \"" + _targetTableName + "_id\" = " + obj.getColumns().get(DBTypeInfo.ID_COLUMN) + " limit 1";
              profiler.start(query);
              ResultSet results = stmt.executeQuery(query);
              try {
                if (results.first()) {
                  Object id = results.getObject(DBTypeInfo.ID_COLUMN);
                  query = "delete from \"" + _joinTableName + "\" where \"id\" = '" + id.toString().replace("'", "''") + "'";
                  profiler.start(query);
                  stmt.executeUpdate(query);
                  _result.remove(obj);
                  return true;
                }
              } finally {
                results.close();
              }
            } else {
              String query = "delete from \"" + _joinTableName + "\" where \"" + _srcTableName + "_id\" = " + _id + " and \"" + _targetTableName + "_id\" = " + obj.getColumns().get(DBTypeInfo.ID_COLUMN);
              profiler.start(query);
              stmt.executeUpdate(query);
              _result.remove(obj);
              return true;
            }
          } finally {
            stmt.close();
          }
        } finally {
          conn.close();
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      } finally {
        profiler.stop();
      }
    }
    return false;
  }

  @Override
  public void clear() {
    Profiler profiler = Util.newProfiler(_srcTableName + "." + _joinTableName + ".clear()");
    String query = "delete from \"" + _joinTableName + "\" where \"" + _srcTableName + "_id\" = " + _id;
    profiler.start(query);
    try {
      Connection conn = _database.getConnection().connect();
      try {
        Statement stmt = conn.createStatement();
        try {
          stmt.executeUpdate(query);
        } finally {
          stmt.close();
        }
      } finally {
        conn.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      profiler.stop();
    }
    _result.clear();
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
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return _result.retainAll(c);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof JoinResult) {
      return o == this || _result.equals(((JoinResult)o)._result);
    } else {
      return o instanceof List && _result.equals(o);
    }
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
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int index, CachedDBObject element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CachedDBObject remove(int index) {
    throw new UnsupportedOperationException();
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
