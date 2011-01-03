package tosa;

import gw.util.GosuExceptionUtil;
import tosa.loader.DBTypeLoader;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBConnection {
  private String _connectURL;
  private ThreadLocal<Connection> _transaction;

  private DBTypeLoader _typeLoader;


  public DBConnection(String connUrl, DBTypeLoader typeLoader) {
    _connectURL = connUrl;
    _transaction = new ThreadLocal<Connection>();
    _typeLoader = typeLoader;
  }

  public Connection connect() throws SQLException {
    Connection trans = _transaction.get();
    if (trans == null) {
      try {
        Class driverClass = Class.forName(getDriverName(_connectURL), true, _typeLoader.getModule().getClassLoader());
        Driver driver = (Driver) (driverClass.newInstance());
        return driver.connect(_connectURL, null);
      } catch (Exception e) {
        throw GosuExceptionUtil.forceThrow(e);
      }
    }
    return trans;
  }

  private static String getDriverName(String url) {
    String dbType = url.split(":")[1];
    if ("h2".equals(dbType)) {
      return "org.h2.Driver";
    }
    if ("mysql".equals(dbType)) {
      return "com.mysql.jdbc.Driver";
    }
    return System.getProperty("db.driver." + dbType);
  }

  public ThreadLocal<Connection> getTransaction() {
    return _transaction;
  }
}
