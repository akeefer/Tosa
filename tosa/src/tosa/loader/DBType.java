package tosa.loader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.util.concurrent.LazyVar;
import tosa.DBConnection;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBType extends TypeBase implements IDBType {

  private DBTypeLoader _typeLoader;
  private LazyVar<DBTypeInfo> _typeInfo;
  private TableTypeData _typeData;

  public DBType(DBTypeLoader dbTypeLoader, TableTypeData typeData) {
    _typeLoader = dbTypeLoader;
    _typeData = typeData;
    _typeInfo = new LazyVar<DBTypeInfo>() {
      @Override
      protected DBTypeInfo init() {
        return new DBTypeInfo(DBType.this);
      }
    };
  }

  TableTypeData getTypeData() {
    return _typeData;
  }

  @Override
  public DBConnection getConnection() {
    return null;
  }

  @Override
  public String getName() {
    return getNamespace() + "." + getRelativeName();
  }

  @Override
  public String getRelativeName() {
    return _typeData.getTableName();
  }

  @Override
  public String getNamespace() {
    return _typeData.getDbTypeData().getNamespace();
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
    return _typeInfo.get();
  }
}
