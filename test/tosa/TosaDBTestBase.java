package tosa;

import gw.lang.reflect.TypeSystem;
import org.junit.Before;
import org.junit.BeforeClass;
import tosa.api.DBLocator;
import tosa.api.IDatabase;
import tosa.dbmd.DatabaseImpl;
import tosa.impl.md.DatabaseImplSource;
import tosa.loader.DBTypeLoader;

import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/8/12
 * Time: 3:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class TosaDBTestBase extends TosaTestBase {

  protected static DatabaseImpl getDB() {
    return (DatabaseImpl) DBLocator.getDatabase("test.testdb");
  }
  
  @BeforeClass
  public static void beforeTestClass() {
    IDatabase database = getDB();
    database.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
    database.getDBUpgrader().recreateTables();
  }

  @Before
  public void beforeTestMethod() {
    deleteAllData();
  }

  private void deleteAllData() {
    clearTable("SelfJoins_join_Baz_Baz");
    clearTable("Relatives_join_Bar_Baz");
    clearTable("\"join_Foo_Baz\"");
    clearTable("\"Baz\"");
    clearTable("Foo");
    clearTable("SortPage");
    clearTable("Bar");
    clearTable("ForOrderByTests");
    clearTable("ForGroupByTests");
    clearTable("ForNumericTests");
  }

  private void clearTable(String tableName) {
    try {
      IDatabase database = DBLocator.getDatabase("test.testdb");
      Connection connection = database.getConnection().connect();
      connection.createStatement().executeUpdate( "DELETE FROM " + tableName );
      connection.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
