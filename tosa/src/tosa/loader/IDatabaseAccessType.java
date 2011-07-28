package tosa.loader;

import tosa.api.IDatabase;
import tosa.dbmd.DatabaseImpl;

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 7/27/11
 * Time: 9:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDatabaseAccessType {
  IDatabase getDatabaseInstance();
}
