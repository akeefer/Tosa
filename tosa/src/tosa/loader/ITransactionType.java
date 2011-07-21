package tosa.loader;

import gw.lang.reflect.IType;
import tosa.api.IDatabase;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ITransactionType extends IType {

  IDatabase getDatabase();

}
