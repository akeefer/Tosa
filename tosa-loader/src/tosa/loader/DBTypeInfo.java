package tosa.loader;

import gw.internal.gosu.parser.expressions.NullExpression;
import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;
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
    
//    createMethod(
//            "fromId",
//            params(param("id", JavaTypes.pLONG(), "the id")),
//            getDBType(),
//            Modifiers.PublicStatic,
//            "Foo",
//            new IMethodCallHandler() {
//              @Override
//              public Object handleCall(Object context, Object... params) {
//                return _delegate.fromId(params[0]);
//              }
//            });
//
//    createMethod(
//            "count",
//            params(param("sql", JavaTypes.STRING(), "the sql for the count query"),
//                   param("params", JavaTypes.MAP().getParameterizedType(JavaTypes.STRING(), JavaTypes.OBJECT()), "parameters", NullExpression.instance())),
//            JavaTypes.pLONG(),
//            Modifiers.PublicStatic,
//            "Foo",
//            new IMethodCallHandler() {
//              @Override
//              public Object handleCall(Object context, Object... params) {
//                return _delegate.count(params[0], params[1]);
//              }
//            });
//
//    createMethod(
//            "countAll",
//            params(),
//            JavaTypes.pLONG(),
//            Modifiers.PublicStatic,
//            "Foo",
//            new IMethodCallHandler() {
//              @Override
//              public Object handleCall(Object context, Object... params) {
//                return _delegate.countAll();
//              }
//            });
//
//    createMethod(
//            "countWhere",
//            params(param("sql", JavaTypes.STRING(), "the sql for the WHERE clause in the count query"),
//                    param("params", JavaTypes.MAP().getParameterizedType(JavaTypes.STRING(), JavaTypes.OBJECT()), "parameters", NullExpression.instance())),
//            JavaTypes.pLONG(),
//            Modifiers.PublicStatic,
//            "Foo",
//            new IMethodCallHandler() {
//              @Override
//              public Object handleCall(Object context, Object... params) {
//                return _delegate.countWhere(params[0], params[1]);
//              }
//            });
//
//    createMethod(
//            "countLike",
//            params(param("template", getDBType(), "the template to use in forming the WHERE clause for the count query")),
//            JavaTypes.pLONG(),
//            Modifiers.PublicStatic,
//            "Foo",
//            new IMethodCallHandler() {
//              @Override
//              public Object handleCall(Object context, Object... params) {
//                return _delegate.countLike(params[0]);
//              }
//            });
    
    /*
  static function fromId(dbType : IDBType, id : long) : IDBObject {
    return new CoreFinder(dbType).fromId(id)
  }

  static function count(dbType : IDBType, sql : String, params : Map<String, Object> = null) : long {
    return new CoreFinder(dbType).count(sql, params)
  }

  static function countAll(dbType : IDBType) : long {
    return new CoreFinder(dbType).countAll()
  }

  static function countWhere(dbType : IDBType, sql : String, params : Map<String, Object> = null) : long {
    return new CoreFinder(dbType).countWhere(sql, params)
  }

  static function countLike(dbType : IDBType, template: IDBObject) : long {
    return new CoreFinder(dbType).countLike(template)
  }

  static function select(dbType : IDBType, sql : String, params : Map<String, Object> = null) : QueryResult<IDBObject> {
    return new CoreFinder(dbType).select(sql, params)
  }

  static function selectAll(dbType : IDBType) : QueryResult<IDBObject> {
    return new CoreFinder(dbType).selectAll()
  }

  static function selectWhere(dbType : IDBType, sql : String, params : Map<String, Object> = null) : QueryResult<IDBObject> {
    return new CoreFinder(dbType).selectWhere(sql, params)
  }

  static function selectLike(dbType : IDBType, template : IDBObject) : QueryResult<IDBObject> {
    return new CoreFinder(dbType).selectLike(template)
  }*/


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
