package tosa.loader;

import gw.lang.reflect.*;
import tosa.CachedDBObject;
import tosa.api.*;
import tosa.dbmd.DBColumnImpl;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBTypeInfo extends TosaBaseTypeInfo implements ITypeInfo {

  public static final String ID_COLUMN = "id";

  public DBTypeInfo(IDBType dbType) {
    super(dbType);

    delegateStaticMethods(TypeSystem.getByFullName("tosa.loader.DBTypeDelegate"));



    for (IDBColumn column : dbType.getTable().getColumns()) {
      // TODO - AHK - Ideally this cast wouldn't be necessary
      addProperty(makeProperty((DBColumnImpl) column));
    }

    for (IDBArray dbArray : getDBType().getTable().getArrays()) {
      addProperty(makeArrayProperty(dbArray));
    }

    addConstructor(new ConstructorInfoBuilder()
        .withConstructorHandler(new IConstructorHandler() {
          @Override
          public Object newInstance(Object... args) {
            return new CachedDBObject(getDBType(), true);
          }
        }).build(this));

    lockDataStructures();
  }

  private DBPropertyInfo makeProperty(DBColumnImpl column) {
    return new DBPropertyInfo(this, column);
  }

  private IPropertyInfo makeArrayProperty(IDBArray dbArray) {
    return new DBArrayPropertyInfo(this, dbArray);
  }

  private IDBType getDBType() {
    return ((IDBType) getOwnersType());
  }

  @Override
  protected IType substituteDelegatedParameterType(IType paramType) {
    return substituteType(paramType);
  }

  @Override
  protected IType substituteDelegatedReturnType(IType returnType) {
    return substituteType(returnType);
  }

  private IType substituteType(IType type) {
    IType dbObjectType = TypeSystem.get(IDBObject.class);
    if (type.equals(dbObjectType)) {
      return getDBType();
    } else if (type.isParameterizedType()) {
      IType[] parameters = type.getTypeParameters();
      IType[] substitutedParameters = new IType[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        substitutedParameters[i] = substituteType(parameters[i]);
      }
      return type.getGenericType().getParameterizedType(substitutedParameters);
    } else {
      return type;
    }
  }

  @Override
  protected Object getFirstArgForDelegatedMethods() {
    return getDBType();
  }
}
