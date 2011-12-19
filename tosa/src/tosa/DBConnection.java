package tosa;

import gw.lang.reflect.IDefaultTypeLoader;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.module.IModule;
import gw.util.GosuExceptionUtil;
import gw.util.concurrent.LockingLazyVar;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import tosa.api.IDBConnection;
import tosa.loader.DBTypeLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBConnection implements IDBConnection {
  private String _connectURL;
  private IModule _module;
  private ThreadLocal<Connection> _transaction;

  private LockingLazyVar<DataSource> _dataSource = new LockingLazyVar() {
    @Override
    protected Object init() {
      return setupDataSource(_connectURL, _module);
    }
  };


  public DBConnection(String connUrl, IModule module) {
    _connectURL = connUrl;
    _transaction = new ThreadLocal<Connection>();
    _module = module;
  }

  @Override
  public Connection connect() throws SQLException {
    Connection trans = _transaction.get();
    if (trans == null) {
      return _dataSource.get().getConnection();
    } else {
      return trans;
    }
  }

  @Override
  public void startTransaction() throws SQLException {
    if (_transaction.get() != null) {
      throw new IllegalStateException("An existing thread-local transaction has already been opened");
    }

    Connection conn = connect();
    conn.setAutoCommit(false);
    ConnectionWrapper wrapper = new ConnectionWrapper(conn);
    _transaction.set(wrapper);
  }

  @Override
  public void commitTransaction() throws SQLException {
    if (_transaction.get() == null) {
      throw new IllegalStateException("No thread-local transaction has been opened");
    }

    _transaction.get().commit();
  }

  @Override
  public void endTransaction() throws SQLException {
    if (_transaction.get() == null) {
      throw new IllegalStateException("No thread-local transaction has been opened");
    }

    // TODO - AHK - This code is suspicious:  I don't believe that the connection will actually get closed here
    Connection conn = _transaction.get();
    conn.rollback();
    conn.close();
    _transaction.set(null);
    conn.setAutoCommit(true);
  }

  @Override
  public String getConnectionURL() {
    return _connectURL;
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

  private DataSource setupDataSource(String connectURI, IModule module) {
    // Ensure the JDBC driver class is loaded
      // TODO - AHK
    final List<? extends ITypeLoader> typeLoaders = module.getTypeLoaders(IDefaultTypeLoader.class);
    ((IDefaultTypeLoader)typeLoaders.get(0)).loadClass(getDriverName(connectURI));

    // TODO - AHK - Figure out the implications of the connection pooling when the jdbc url changes

    GenericObjectPool connectionPool = new GenericObjectPool(null);
    connectionPool.setMinIdle( 1 );
    connectionPool.setMaxActive( 10 );

    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI,null);
    PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);

    return new PoolingDataSource(connectionPool);
  }
}
