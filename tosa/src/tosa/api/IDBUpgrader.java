package tosa.api;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 2/18/11
 * Time: 11:15 PM
 * To change this template use File | Settings | File Templates.
 */
// TODO - AHK - There's probably a more appropriate name for this interface
public interface IDBUpgrader {

  // TODO - AHK- Kill this interface, fold it all into just Database
  void recreateTables();

  void createTables();

  void dropTables();
}
