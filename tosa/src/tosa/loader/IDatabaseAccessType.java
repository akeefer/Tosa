package tosa.loader;

import gw.lang.reflect.IType;
import tosa.api.IDatabase;

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 7/27/11
 * Time: 9:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDatabaseAccessType extends IType {
  IDatabase getDatabaseInstance();
}
