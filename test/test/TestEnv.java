package test;

import gw.lang.reflect.TypeSystem;
import gw.lang.shell.Gosu;
import tosa.api.DBLocator;
import tosa.api.IDatabase;
import tosa.loader.DBTypeLoader;

public class TestEnv {
  private static boolean _inited = false;
  public static synchronized void maybeInit() {
    if (!_inited) {
      Gosu.init();
      TypeSystem.pushTypeLoader(new DBTypeLoader());
      IDatabase database = DBLocator.getDatabase("test.testdb");
      database.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
      _inited = true;
    }
  }

}
