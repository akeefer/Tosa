package tosa.api;

import gw.lang.reflect.TypeSystem;
import tosa.loader.DBTypeLoader;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/9/11
 * Time: 4:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBLocator {
  // TODO - AHK - Think about this class name
  // TODO - AHK - Think about this API

  public static IDatabase getDatabase() {
    DBTypeLoader dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader.class);
    Collection<? extends IDatabase> allDatabases = dbTypeLoader.getAllDatabases();
    if (allDatabases.size() == 0) {
      throw new IllegalArgumentException("No databases found");
    } else if (allDatabases.size() > 2) {
      throw new IllegalArgumentException("getDatabase() can only be called if there is a single IDatabase instance.  Found " + allDatabases.size());
    } else {
      return allDatabases.iterator().next();
    }
  }

  public static IDatabase getDatabase(String packageName) {
    DBTypeLoader dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader.class);
    return dbTypeLoader.getTypeDataForNamespace( "test.testdb" );
  }
}
