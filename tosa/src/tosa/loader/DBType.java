package tosa.loader;

import gw.lang.reflect.*;
import gw.util.concurrent.LazyVar;
import tosa.api.IDBObject;
import tosa.api.IDBTable;
import tosa.api.query.CoreFinder;
import tosa.dbmd.DBTableImpl;
import tosa.impl.query.CoreFinderImpl;
import tosa.impl.query.NewQueryExecutor;
import tosa.impl.query.NewQueryExecutorImpl;

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
  private CoreFinder _finder;

  public DBType(DBTypeLoader dbTypeLoader, IDBTable table) {
    _typeLoader = dbTypeLoader;
    _table = table;
    _typeInfo = new LazyVar<DBTypeInfo>() {
      @Override
      protected DBTypeInfo init() {
        return new DBTypeInfo(getTypeReference());
      }
    };
    _finder = new CoreFinderImpl<IDBObject>(getTypeReference());
  }

  public IDBType getTypeReference() {
    return (IDBType) TypeSystem.getOrCreateTypeReference(DBType.this);
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
    return Collections.singletonList(TypeSystem.get(IDBObject.class));
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return _typeInfo.get();
  }

  public CoreFinder getFinder() {
    return _finder;
  }

  @Override
  public NewQueryExecutor getNewQueryExecutor() {
    // TODO - AHK - Cache this?
    return new NewQueryExecutorImpl(_table.getDatabase());
  }
}
