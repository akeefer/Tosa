package tosa;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuObject;
import org.slf4j.profiler.Profiler;
import tosa.api.IDBColumn;
import tosa.api.IDatabase;
import tosa.api.IPreparedStatementParameter;
import tosa.loader.DBTypeInfo;
import tosa.loader.IDBType;
import tosa.loader.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CachedDBObject implements IGosuObject {
  private Map<String, Object> _columns;
  private IDBType _type;
  private boolean _new;

  @Override
  public IType getIntrinsicType() {
    return _type;
  }

  public String getTableName() {
    return _type.getRelativeName();
  }

  public boolean isNew() {
    return _new;
  }

  public Map<String, Object> getColumns() {
    return _columns;
  }

  public CachedDBObject(IDBType type, boolean isNew) {
    // TODO - AHK
    _type = (IDBType) TypeSystem.getOrCreateTypeReference(type);
    _new = isNew;
    _columns = new HashMap<String, Object>();
  }

  public void update() throws SQLException {
    Profiler profiler = Util.newProfiler(_type.getName() + ".update()");
    IDatabase database = _type.getTable().getDatabase();
    List<String> attrs = new ArrayList<String>();
    List<IPreparedStatementParameter> values = new ArrayList<IPreparedStatementParameter>();
    for (Map.Entry<String, Object> entry : _columns.entrySet()) {
      if (entry.getKey().equals(DBTypeInfo.ID_COLUMN)) {
        continue;
      }
      IDBColumn column = _type.getTable().getColumn(entry.getKey());
      if (column != null) {
        attrs.add("\"" + entry.getKey() + "\"");
        Object value = entry.getValue();
        if (column.isFK() && value instanceof CachedDBObject) {
          value = ((CachedDBObject) value).getColumns().get(DBTypeInfo.ID_COLUMN);
        }
        values.add(database.wrapParameter(value, column));
      }
    }
    if (_new) {
      StringBuilder query = new StringBuilder("insert into \"");
      query.append(getTableName()).append("\" (");
      for (String key : attrs) {
        query.append(key);
        if (key != attrs.get(attrs.size() - 1)) {
          query.append(", ");
        }
      }
      query.append(") values (");
      for (int i = 0; i < attrs.size(); i++) {
        if (i > 0) {
          query.append(", ");
        }
        query.append("?");
      }
      query.append(")");
      profiler.start(query.toString() + " (" + values + ")");
      Object id = database.executeInsert(query.toString(), values.toArray(new IPreparedStatementParameter[values.size()]));
      if (id != null) {
        _columns.put(DBTypeInfo.ID_COLUMN, id);
        _new = false;
      }
    } else {
      StringBuilder query = new StringBuilder("update \"");
      query.append(getTableName()).append("\" set ");
      for (String attr : attrs) {
        query.append(attr).append(" = ?");
        if (attr != attrs.get(attrs.size() - 1)) {
          query.append(", ");
        }
      }
      query.append(" where \"id\" = ?");
      profiler.start(query.toString() + " (" + values + ")");
      values.add(database.wrapParameter(_columns.get(DBTypeInfo.ID_COLUMN), _type.getTable().getColumn(DBTypeInfo.ID_COLUMN)));
      database.executeInsert(query.toString(), values.toArray(new IPreparedStatementParameter[values.size()]));
    }
    profiler.stop();
  }

  public void delete() throws SQLException {
    Profiler profiler = Util.newProfiler(_type.getName() + ".delete()");
    String query = "delete from \"" + getTableName() + "\" where \"id\" = '" + (_columns.get(DBTypeInfo.ID_COLUMN).toString().replace("'", "''")) + "'";
    profiler.start(query);
    Connection conn = _type.getTable().getDatabase().getConnection().connect();
    try {
      Statement stmt = conn.createStatement();
      try {
        stmt.execute(query);
      } finally {
        stmt.close();
      }
    } finally {
      conn.close();
      profiler.stop();
    }
  }

  @Override
  public String toString() {
    return _columns.toString();
  }

  @Override
  public int hashCode() {
    int hashCode = _type.hashCode();
    List<String> keys = new ArrayList<String>(_columns.keySet());
    Collections.sort(keys);
    for (String columnName : keys) {
      if (_columns.get(columnName) != null) {
        hashCode = hashCode * 17 + _columns.get(columnName).hashCode();
      } else {
        hashCode *= 17;
      }
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CachedDBObject) {
      CachedDBObject other = (CachedDBObject) obj;
      if (_type.equals(other._type)) {
        for (String columnName : _columns.keySet()) {
          if (_columns.get(columnName) != null) {
            if (!_columns.get(columnName).equals(other._columns.get(columnName))) {
              return false;
            }
          }
        }
        for (String columnName : other._columns.keySet()) {
          if (other._columns.get(columnName) != null) {
            if (!other._columns.get(columnName).equals(_columns.get(columnName))) {
              return false;
            }
          }
        }
        return true;
      }
    }
    return false;
  }

}
