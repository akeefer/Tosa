package test;

import tosa.api.DBLocator;
import tosa.api.IDatabase;

public class TestEnv {

  static {
    IDatabase database = DBLocator.getDatabase("test.testdb");
    database.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
  }

  public static synchronized void init() { /* forces static init */ }
}
