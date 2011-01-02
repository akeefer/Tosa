package tosa.loader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import tosa.DBConnection;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionType extends TypeBase implements ITransactionType {

  public static final String TYPE_NAME = "Transaction";

  private DBTypeData _dbTypeData;
  private TransactionTypeInfo _typeInfo;
  private DBTypeLoader _typeLoader;

  public TransactionType(DBTypeData dbTypeData, DBTypeLoader dbTypeLoader) {
    _dbTypeData = dbTypeData;
    _typeLoader = dbTypeLoader;
    _typeInfo = new TransactionTypeInfo(this);
  }

  @Override
  public String getName() {
    return _dbTypeData.getNamespace() + "." + TYPE_NAME;
  }

  @Override
  public String getRelativeName() {
    return TYPE_NAME;
  }

  @Override
  public String getNamespace() {
    return _dbTypeData.getNamespace();
  }

  @Override
  public ITypeLoader getTypeLoader() {
    return _typeLoader;
  }

  @Override
  public IType getSupertype() {
    return null;
  }

  @Override
  public List<? extends IType> getInterfaces() {
    return Collections.emptyList();
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return _typeInfo;
  }

  public DBConnection getConnection() {
//    return _conn;
    return null;
  }

}
