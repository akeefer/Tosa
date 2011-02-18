package tosa;

import tosa.api.*;
import org.slf4j.profiler.Profiler;
import tosa.api.IDatabase;
import tosa.loader.DBTypeInfo;
import tosa.loader.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
  private IDBColumn _idColumn;
  private IDBColumn _srcColumn;
  private IDBColumn _targetColumn;
  private String _srcId;

  public JoinResult(List<CachedDBObject> result, IDatabase database, IDBTable joinTable,
                    IDBColumn srcColumn, IDBColumn targetColumn, String srcId) {
    _result = result;
    _database = database;
    _joinTable = joinTable;
    _idColumn = _joinTable.getColumn(DBTypeInfo.ID_COLUMN);
    _srcColumn = srcColumn;
    _targetColumn = targetColumn;
    _srcId = srcId;
  }

  @Override
  public boolean add(CachedDBObject obj) {
    // TODO - AHK - Determine dynamically if table names should be quoted or not
    Profiler profiler = Util.newProfiler(_srcColumn.getTable().getName() + "." + _joinTable.getName() + ".add()");
    String query = "insert into \"" + _joinTable.getName() + "\" (\"" + _srcColumn.getName() + "\", \"" + _targetColumn.getName() + "\") values (?, ?)";
    try {
      IDBExecutionKernel.IPreparedStatementParameter[] parameters = new IDBExecutionKernel.IPreparedStatementParameter[2];
      parameters[0] = _database.wrapParameter(_srcId, _srcColumn);
      parameters[1] = _database.wrapParameter(obj.getColumns().get(DBTypeInfo.ID_COLUMN), _targetColumn);
      profiler.start(query + "( " + parameters[0] + ", " + parameters[1] + ")");
      _database.getDBExecutionKernel().executeInsert(query, parameters);
    } finally {
      profiler.stop();
    }
    _result.add(obj);
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends CachedDBObject> objs) {
    List<IDBExecutionKernel.IPreparedStatementParameter> parameters = new ArrayList<IDBExecutionKernel.IPreparedStatementParameter>();
    StringBuilder query = new StringBuilder("insert into \"");
    query.append(_joinTable.getName()).append("\" (\"").append(_srcColumn.getName()).append("\", \"").append(_targetColumn.getName()).append("\") values ");
    for (CachedDBObject obj : objs) {
      parameters.add(_database.wrapParameter(_srcId, _srcColumn));
      parameters.add(_database.wrapParameter(obj.getColumns().get(DBTypeInfo.ID_COLUMN), _targetColumn));
      query.append("(?, ?)");
      query.append(", ");
    }
    if (!objs.isEmpty()) {
      query.setLength(query.length() - 2);
    }
    Profiler profiler = Util.newProfiler(_srcColumn.getTable().getName() + "." + _joinTable.getName() + ".addAll()");
    profiler.start(query.toString() + " (" + parameters + ")");
    try {
    _database.getDBExecutionKernel().executeInsert(query.toString(), parameters.toArray(new IDBExecutionKernel.IPreparedStatementParameter[parameters.size()]));
    } finally {
      profiler.stop();
    }
    _result.addAll(objs);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    Profiler profiler = Util.newProfiler(_srcColumn.getTable().getName() + "." + _joinTable.getName() + ".remove()");
    if (o instanceof CachedDBObject) {
      CachedDBObject obj = (CachedDBObject) o;
      try {
        List<IDBExecutionKernel.IPreparedStatementParameter> parameters = new ArrayList<IDBExecutionKernel.IPreparedStatementParameter>();
        parameters.add(_database.wrapParameter(_srcId, _srcColumn));
        parameters.add(_database.wrapParameter(obj.getColumns().get(DBTypeInfo.ID_COLUMN), _targetColumn));
        if (_database.getTable(_joinTable.getName()).hasId()) {
          String query = "select * from \"" + _joinTable.getName() + "\" where \"" + _srcColumn.getName() + "\" = ? and \"" + _targetColumn.getName() + "\" = ? limit 1";
          profiler.start(query + " (" + parameters + ")");
          List<Object> results = _database.getDBExecutionKernel().executeSelect(query, new JoinQueryResultProcessor(),
              parameters.toArray(new IDBExecutionKernel.IPreparedStatementParameter[parameters.size()]));
          if (!results.isEmpty() && results.get(0) != null) {
            parameters.clear();
            parameters.add(_database.wrapParameter(results.get(0), _idColumn));
            query = "delete from \"" + _joinTable.getName() + "\" where \"id\" = ?";
            profiler.start(query + " (" + parameters + ")");
            _database.getDBExecutionKernel().executeDelete(query, parameters.toArray(new IDBExecutionKernel.IPreparedStatementParameter[parameters.size()]));
            _result.remove(obj);
            return true;
          }
        } else {
          String query = "delete from \"" + _joinTable.getName() + "\" where \"" + _srcColumn.getName() + "\" = ? and \"" + _targetColumn.getName() + "\" = ?";
          profiler.start(query + " (" + parameters + ")");
          _database.getDBExecutionKernel().executeDelete(query, parameters.toArray(new IDBExecutionKernel.IPreparedStatementParameter[parameters.size()]));
          _result.remove(obj);
          return true;
        }
      } finally {
        profiler.stop();
      }
    }
    return false;
  }

  @Override
  public void clear() {
    Profiler profiler = Util.newProfiler(_srcColumn.getTable().getName() + "." + _joinTable.getName() + ".clear()");
    String query = "delete from \"" + _joinTable.getName() + "\" where \"" + _srcColumn.getName() + "\" = ?";
    IDBExecutionKernel.IPreparedStatementParameter parameter = _database.wrapParameter(_srcId, _srcColumn);
    profiler.start(query + " (" + parameter + ")");
    try {
      _database.getDBExecutionKernel().executeDelete(query, parameter);
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

  private static class JoinQueryResultProcessor implements IDBExecutionKernel.IQueryResultProcessor<Object> {
    @Override
    public Object processResult(ResultSet result) throws SQLException {
      return result.getObject(DBTypeInfo.ID_COLUMN);
    }
  }
}
