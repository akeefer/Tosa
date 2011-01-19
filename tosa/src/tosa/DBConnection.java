package tosa;

import gw.util.GosuExceptionUtil;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import tosa.loader.DBTypeLoader;

import javax.sql.DataSource;
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
  private DataSource _dataSource;


  public DBConnection(String connUrl, DBTypeLoader typeLoader) {
    _connectURL = connUrl;
    _transaction = new ThreadLocal<Connection>();
    _typeLoader = typeLoader;
    _dataSource = setupDataSource(connUrl);
  }

  public Connection connect() throws SQLException {
    Connection trans = _transaction.get();
    if (trans == null) {
      return _dataSource.getConnection();
    } else {
      return trans;
    }
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

  public DataSource setupDataSource(String connectURI) {
    // Ensure the JDBC driver class is loaded
    try {
      Class.forName(getDriverName(_connectURL), true, _typeLoader.getModule().getClassLoader());
    } catch (ClassNotFoundException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }

    GenericObjectPool connectionPool = new GenericObjectPool(null);
    connectionPool.setMinIdle( 1 );
    connectionPool.setMaxActive( 10 );

    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI,null);
    PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);

    return new PoolingDataSource(connectionPool);
  }
}
