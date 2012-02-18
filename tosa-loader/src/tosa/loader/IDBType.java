package tosa.loader;

import gw.lang.reflect.IEnhanceableType;
import gw.lang.reflect.IProvidesCustomErrorInfo;
import tosa.api.IDBTable;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBType extends IEnhanceableType, IProvidesCustomErrorInfo {
  IDBTable getTable();
}
