package tosa.loader.data;

import gw.lang.reflect.module.IModule;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 11:02 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBDataSource {

  /**
   * Retrieves all of the DBData for the given module.  The resulting map will map
   * a namespace to a DBData object.
   */
  Map<String, DBData> getDBData(IModule module);
}
