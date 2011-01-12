package tosa.query;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.features.PropertyReference;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuStringUtil;
import tosa.CachedDBObject;
import tosa.api.IDBColumn;
import tosa.api.IDBTable;
import tosa.api.IDatabase;
import tosa.api.IQueryResultProcessor;
import tosa.loader.DBPropertyInfo;
import tosa.loader.DBType;
import tosa.loader.IDBType;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/11/11
 * Time: 9:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectHelper {

  public static CachedDBObject selectById(IDBType entityType, Object id) throws SQLException {
    // TODO - AHK - Input validation (i.e. id should not be null)
    IDBTable table = entityType.getTable();
    IDBColumn idColumn = table.getColumn("id"); // TODO - AHK - Make the colum name a constant
    IDatabase db = entityType.getTable().getDatabase();

    // TODO - AHK - Use some DB-aware utility to decide when to quote things, etc.
    // TODO - AHK - Make the colum name a constant
    List<CachedDBObject> results = db.executeSelect("select * from \"" + table.getName() + "\" where \"id\" = ?",
        new CachedDBQueryResultProcessor(entityType),
        db.wrapParameter(id, idColumn));

    if (results.size() == 0) {
      return null;
    } else if (results.size() == 1) {
      return results.get(0);
    } else {
      throw new IllegalStateException("More than one row in table " + table.getName() + " had id " + id);
    }
  }

  private static class CachedDBQueryResultProcessor implements IQueryResultProcessor<CachedDBObject> {
    private IDBType _type;

    private CachedDBQueryResultProcessor(IDBType type) {
      _type = type;
    }

    @Override
    public CachedDBObject processResult(ResultSet result) throws SQLException {
      return buildObject(_type, result);
    }
  }

//  public static List<CachedDBObject> findFromTemplate(CachedDBObject template, PropertyReference sortColumn, boolean ascending, int limit, int offset) throws SQLException {
//    StringBuilder query = new StringBuilder("select * from \"");
//    query.append(getOwnersType().getRelativeName()).append("\" where ");
//    addWhereClause(query, template);
//    if (sortColumn != null) {
//      query.append(" order by \"").append(sortColumn.getPropertyInfo().getName()).append("\" ").append(ascending ? "ASC" : "DESC").append(", \"id\" ASC");
//    } else {
//      query.append(" order by \"id\" ASC");
//    }
//    if (limit != -1) {
//      query.append(" limit ").append(limit).append(" offset ").append(offset);
//    }
//    return findFromSql(query.toString());
//  }
//
//  public static int countFromTemplate(CachedDBObject template) throws SQLException {
//    StringBuilder query = new StringBuilder("select count(*) as count from \"").append(getOwnersType().getRelativeName()).append("\" where ");
//    addWhereClause(query, template);
//    return countFromSql(query.toString());
//  }
//
//  public static int countFromSql(String query) throws SQLException {
//    Connection conn = connect();
//    try {
//      Statement stmt = conn.createStatement();
//      try {
//        stmt.executeQuery(query);
//        ResultSet result = stmt.getResultSet();
//        try {
//          if (result.first()) {
//            return result.getInt("count");
//          } else {
//            return 0;
//          }
//        } finally {
//          result.close();
//        }
//      } finally {
//        stmt.close();
//      }
//    } finally {
//      conn.close();
//    }
//  }
//
//  public static void addWhereClause(StringBuilder query, CachedDBObject template) {
//    List<String> whereClause = new ArrayList<String>();
//    if (template != null) {
//      for (Map.Entry<String, Object> column : template.getColumns().entrySet()) {
//        if (column.getValue() != null) {
//          String value = "'" + column.getValue().toString().replace("'", "''") + "'";
//          whereClause.add("\"" + column.getKey() + "\" = " + value);
//        }
//      }
//      if (!whereClause.isEmpty()) {
//        query.append(GosuStringUtil.join(whereClause, " and "));
//      } else {
//        query.append("true");
//      }
//    } else {
//      query.append("true");
//    }
//  }
//
//  public static List<CachedDBObject> findInDb(List<IPropertyInfo> props, Object... args) throws SQLException {
//    List<String> whereClause = new ArrayList<String>();
//    for (int i = 0; i < props.size(); i++) {
//      IPropertyInfo p = props.get(i);
//      if (p instanceof DBPropertyInfo) {
//        DBPropertyInfo dbProperty = (DBPropertyInfo) p;
//        String value;
//        if (dbProperty.getColumnName().endsWith("_id")) {
//          value = ((CachedDBObject) args[i]).getColumns().get("id").toString();
//        } else {
//          value = "'" + args[i].toString().replace("'", "''") + "'";
//        }
//        whereClause.add("\"" + dbProperty.getColumnName() + "\" = " + value);
//      }
//    }
//    return findFromSql("select * from \"" + getOwnersType().getRelativeName() + "\" where " + GosuStringUtil.join(whereClause, " and "));
//  }
//
//  public static List<CachedDBObject> findFromSql(String query) throws SQLException {
//    List<CachedDBObject> objs = new ArrayList<CachedDBObject>();
//    Connection conn = connect();
//    try {
//      Statement stmt = conn.createStatement();
//      try {
//        stmt.executeQuery(query);
//        ResultSet result = stmt.getResultSet();
//        try {
//          if (result.first()) {
//            objs = buildObjects(result);
//          }
//        } finally {
//          result.close();
//        }
//      } finally {
//        stmt.close();
//      }
//    } finally {
//      conn.close();
//    }
//    return Collections.unmodifiableList(objs);
//  }
//
//  public static ArrayList<CachedDBObject> buildObjects(ResultSet result) throws SQLException {
//    ArrayList<CachedDBObject> objs = new ArrayList<CachedDBObject>();
//    while (!result.isAfterLast()) {
//      objs.add(buildObject(result));
//      result.next();
//    }
//    return objs;
//  }
//
  public static CachedDBObject buildObject(IDBType objectType, ResultSet result) throws SQLException {
    CachedDBObject obj = new CachedDBObject(objectType, false);
    // TODO - AHK - This should probably just iterate over columns rather than properties
    for (IPropertyInfo prop : objectType.getTypeInfo().getProperties()) {
      if (prop instanceof DBPropertyInfo) {
        DBPropertyInfo dbProp = (DBPropertyInfo) prop;
        // TODO - AHK - This should get the IDBColumn off the property instead of having a getColumnName method on it
        Object resultObject = result.getObject(objectType.getTable().getName() + "." + dbProp.getColumnName());
        if (resultObject instanceof BufferedReader) {
          obj.getColumns().put(dbProp.getColumnName(), readAll((BufferedReader) resultObject));
        } else if (resultObject instanceof Clob) {
          obj.getColumns().put(dbProp.getColumnName(), readAll(new BufferedReader(((Clob) resultObject).getCharacterStream())));
        } else if (dbProp.getFeatureType().equals(IJavaType.pBOOLEAN) && resultObject == null) {
          obj.getColumns().put(dbProp.getColumnName(), Boolean.FALSE);
        } else {
          obj.getColumns().put(dbProp.getColumnName(), resultObject);
        }
      }
    }
    return obj;
  }

  private static Object readAll(BufferedReader r) {
    try {
      StringBuilder b = new StringBuilder();
      String line = r.readLine();
      while (line != null) {
        b.append(line).append("\n");
        line = r.readLine();
      }
      if (b.length() > 0) {
        b.setLength(b.length() - 1);
      }
      return b.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
