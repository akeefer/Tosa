package tosa.api.tools;

import gw.fs.IDirectory;
import gw.lang.init.GosuInitialization;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeSystem;
import gw.lang.Gosu;
import gw.util.GosuExceptionUtil;
import tosa.DBConnection;
import tosa.api.DBLocator;
import tosa.api.IDatabase;
import tosa.loader.DBTypeLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 3/24/11
 * Time: 10:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class DBManagementTool {

  public static void main(String[] args) {
    Gosu.init();

    if (args.length == 1) {
      if (args[0].equals("create")) {
        createDatabase();
      } else if (args[0].equals("delete")) {
        deleteDatabase();
      } else if (args[0].equals("recreate")) {
        recreateDatabase();
      } else {
        System.out.println("ERROR: expected one of create, delete, or recreate");
      }
    } else if (args.length == 2) {
      if (args[0].equals("create")) {
        createDatabase(args[1]);
      } else if (args[0].equals("delete")) {
        deleteDatabase(args[1]);
      } else if (args[0].equals("recreate")) {
        recreateDatabase(args[1]);
      } else {
        System.out.println("ERROR: expected one of create, delete, or recreate");
      }
    } else {
      System.out.println("The DBManagementTool's main function requires arguments of the form <op> <package>");
    }
  }

  public static void createDatabase() {
    createDatabase(DBLocator.getDatabase());
  }

  public static void createDatabase(String packageName) {
    createDatabase(DBLocator.getDatabase(packageName));
  }

  private static void createDatabase(IDatabase db) {
    getDBSpecificTool(db.getConnection().getConnectionURL()).createDatabase(db);
  }

  public static void deleteDatabase() {
    deleteDatabase(DBLocator.getDatabase());
  }

  public static void deleteDatabase(String packageName) {
    deleteDatabase(DBLocator.getDatabase(packageName));
  }

  private static void deleteDatabase(IDatabase db) {
    getDBSpecificTool(db.getConnection().getConnectionURL()).deleteDatabase(db);
  }

  public static void recreateDatabase() {
    recreateDatabase(DBLocator.getDatabase());
  }

  public static void recreateDatabase(String packageName) {
    recreateDatabase(DBLocator.getDatabase(packageName));
  }

  private static void recreateDatabase(IDatabase db) {
    deleteDatabase(db);
    createDatabase(db);
  }

  private static DBSpecificManagementTool getDBSpecificTool(String connectionURL) {
    String[] urlParts = connectionURL.split(":");
    String dbType = urlParts[1];
    if (dbType.equals("mysql")) {
      return new MySQLDBManagementTool();
    } else {
      throw new RuntimeException("Currently the dbType " + dbType + " is not supported by the DBManagementTool");
    }
  }

  private interface DBSpecificManagementTool {
    void deleteDatabase(IDatabase db);
    void createDatabase(IDatabase db);
  }

  private static class MySQLDBManagementTool implements DBSpecificManagementTool {
    public void createDatabase(IDatabase db) {
      // TODO - AHK - Refactor
      DBConnectionInfo dbConnectionInfo = parseConnectionURI(db.getConnection().getConnectionURL());
      System.out.println("Creating database " + dbConnectionInfo.getDbName());
      String newConnectionURI = dbConnectionInfo.getBaseURL() + "/mysql?" + dbConnectionInfo.getAttributes();
      try {
        Class.forName("com.mysql.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        GosuExceptionUtil.forceThrow(e);
      }
      try {
        Connection connection = DriverManager.getConnection(newConnectionURI);
        Statement st = connection.createStatement();
        st.executeUpdate("CREATE DATABASE " + dbConnectionInfo.getDbName());
        connection.close();
        System.out.println("Database " + dbConnectionInfo.getDbName() + " successfully created");
        db.getDBUpgrader().createTables();
        System.out.println("Tables for " + dbConnectionInfo.getDbName() + " successfully created");
      } catch (SQLException e) {
        GosuExceptionUtil.forceThrow(e);
      }
    }

    public void deleteDatabase(IDatabase db) {
      DBConnectionInfo dbConnectionInfo = parseConnectionURI(db.getConnection().getConnectionURL());
      System.out.println("Dropping database " + dbConnectionInfo.getDbName());
      String newConnectionURI = dbConnectionInfo.getBaseURL() + "/mysql?" + dbConnectionInfo.getAttributes();
      try {
        Class.forName("com.mysql.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        GosuExceptionUtil.forceThrow(e);
      }
      try {
        Connection connection = DriverManager.getConnection(newConnectionURI);
        Statement st = connection.createStatement();
        st.executeUpdate("DROP DATABASE " + dbConnectionInfo.getDbName());
        connection.close();
        System.out.println("Database " + dbConnectionInfo.getDbName() + " successfully dropped");
      } catch (SQLException e) {
        GosuExceptionUtil.forceThrow(e);
      }
    }

    private DBConnectionInfo parseConnectionURI(String connectionURI) {
      // jdbc:mysql://localhost:3306/reposervice?user=root&password=p@ssword&sessionVariables=sql_mode=ANSI_QUOTES
      int addressStart = connectionURI.indexOf("://");
      int addressEnd = connectionURI.indexOf("/", addressStart + 3);
      String baseURL = connectionURI.substring(0, addressEnd);
      int paramStart = connectionURI.indexOf("?", addressEnd + 1);
      String dbName = connectionURI.substring(addressEnd + 1, paramStart);
      String attributes = connectionURI.substring(paramStart + 1);
      return new DBConnectionInfo(baseURL, dbName, attributes);
    }
  }

  private static class DBConnectionInfo {
    private String _baseURL;
    private String _dbName;
    private String _attributes;

    private DBConnectionInfo(String baseURL, String dbName, String attributes) {
      _baseURL = baseURL;
      _dbName = dbName;
      _attributes = attributes;
    }

    public String getBaseURL() {
      return _baseURL;
    }

    public String getDbName() {
      return _dbName;
    }

    public String getAttributes() {
      return _attributes;
    }

    @Override
    public String toString() {
      return "_baseURL=[" + _baseURL + "], _dbName=[" + _dbName + "], _attributes=[" + _attributes + "]";
    }
  }
}
