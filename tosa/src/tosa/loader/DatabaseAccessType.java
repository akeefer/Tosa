package tosa.loader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.TypeSystem;
import tosa.api.IDatabase;
import tosa.dbmd.DatabaseImpl;

import java.util.Collections;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseAccessType extends TypeBase implements IDatabaseAccessType {

  public static final String TYPE_NAME = "Database";

  private DatabaseImpl _databaseImpl;
  private DatabaseAccessTypeInfo _typeInfo;
  private DBTypeLoader _typeLoader;

  public DatabaseAccessType(DatabaseImpl databaseImpl, DBTypeLoader dbTypeLoader) {
    _databaseImpl = databaseImpl;
    _typeLoader = dbTypeLoader;
    _typeInfo = new DatabaseAccessTypeInfo(this);
  }

  @Override
  public String getName() {
    return _databaseImpl.getNamespace() + "." + TYPE_NAME;
  }

  @Override
  public String getRelativeName() {
    return TYPE_NAME;
  }

  @Override
  public String getNamespace() {
    return _databaseImpl.getNamespace();
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
    return Collections.singletonList(TypeSystem.get(DatabaseAccessTypeMarker.class));
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return _typeInfo;
  }

  public IDatabase getDatabaseInstance() {
    return _databaseImpl;
  }
}
