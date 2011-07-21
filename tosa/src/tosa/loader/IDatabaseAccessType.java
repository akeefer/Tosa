package tosa.loader;

import gw.lang.reflect.IType;
import tosa.api.IDatabase;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 7/21/11
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDatabaseAccessType extends IType {
  IDatabase getDatabase();
}
