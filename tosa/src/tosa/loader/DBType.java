package tosa.loader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.util.concurrent.LazyVar;
import tosa.api.IDBTable;
import tosa.dbmd.DBTableImpl;

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
  private IDBTable _table;

  public DBType(DBTypeLoader dbTypeLoader, IDBTable table) {
    _typeLoader = dbTypeLoader;
    _table = table;
    _typeInfo = new LazyVar<DBTypeInfo>() {
      @Override
      protected DBTypeInfo init() {
        return new DBTypeInfo(DBType.this);
      }
    };
  }

  public IDBTable getTable() {
    return _table;
  }

  @Override
  public String getName() {
    return getNamespace() + "." + getRelativeName();
  }

  @Override
  public String getRelativeName() {
    return _table.getName();
  }

  @Override
  public String getNamespace() {
    return _table.getDatabase().getNamespace();
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
