package tosa.loader;

import gw.internal.gosu.parser.expressions.NullExpression;
import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IConstructorHandler;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.JavaTypes;
import gw.util.concurrent.LockingLazyVar;
import tosa.api.IDBArray;
import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.dbmd.DBColumnImpl;
import tosa.impl.RuntimeBridge;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBTypeInfo extends TosaBaseTypeInfo implements ITypeInfo {

  public static final String ID_COLUMN = "id";

  private LockingLazyVar<DBTypeInfoDelegate> _delegate = new LockingLazyVar<DBTypeInfoDelegate>() {
    @Override
    protected DBTypeInfoDelegate init() {
      return RuntimeBridge.createTypeInfoDelegate();
    }
  };

  private DBTypeInfoDelegate getDelegate() {
    return _delegate.get();
  }
  
  public DBTypeInfo(IDBType dbType) {
    super(dbType);
    
    createMethod(
            "fromId",
            params(param("id", JavaTypes.pLONG(), "the id of the object to find")),
            getDBType(),
            Modifiers.PublicStatic,
            "TODO",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object context, Object... params) {
                return getDelegate().fromId(getDBType(), (Long) params[0]);
              }
            });

    createMethod(
            "count",
            params(param("sql", JavaTypes.STRING(), "the sql for the count query"),
                   param("params", JavaTypes.MAP().getParameterizedType(JavaTypes.STRING(), JavaTypes.OBJECT()), "parameters", NullExpression.instance())),
            JavaTypes.pLONG(),
            Modifiers.PublicStatic,
            "TODO",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object context, Object... params) {
                return getDelegate().count(getDBType(), (String) params[0], (Map<String, Object>) params[1]);
              }
            });

    createMethod(
            "countAll",
            params(),
            JavaTypes.pLONG(),
            Modifiers.PublicStatic,
            "TODO",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object context, Object... params) {
                return getDelegate().countAll(getDBType());
              }
            });

    createMethod(
            "countWhere",
            params(param("sql", JavaTypes.STRING(), "the sql for the WHERE clause in the count query"),
                    param("params", JavaTypes.MAP().getParameterizedType(JavaTypes.STRING(), JavaTypes.OBJECT()), "parameters", NullExpression.instance())),
            JavaTypes.pLONG(),
            Modifiers.PublicStatic,
            "TODO",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object context, Object... params) {
                return getDelegate().countWhere(getDBType(), (String) params[0], (Map<String, Object>) params[1]);
              }
            });

    createMethod(
            "countLike",
            params(param("template", getDBType(), "the template to use in forming the WHERE clause for the count query")),
            JavaTypes.pLONG(),
            Modifiers.PublicStatic,
            "TODO",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object context, Object... params) {
                return getDelegate().countLike(getDBType(), (IDBObject) params[0]);
              }
            });

    createMethod(
            "select",
            params(param("sql", JavaTypes.STRING(), "the sql for the query"),
                    param("params", JavaTypes.MAP().getParameterizedType(JavaTypes.STRING(), JavaTypes.OBJECT()), "parameters", NullExpression.instance())),
            TypeSystem.getByFullName("tosa.api.QueryResult").getParameterizedType(getDBType()),
            Modifiers.PublicStatic,
            "TODO",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object context, Object... params) {
                return getDelegate().select(getDBType(), (String) params[0], (Map<String, Object>) params[1]);
              }
            });

    createMethod(
            "selectAll",
            params(),
            TypeSystem.getByFullName("tosa.api.QueryResult").getParameterizedType(getDBType()),
            Modifiers.PublicStatic,
            "TODO",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object context, Object... params) {
                return getDelegate().selectAll(getDBType());
              }
            });

    createMethod(
            "selectWhere",
            params(param("sql", JavaTypes.STRING(), "the sql for the WHERE clause of the query"),
                    param("params", JavaTypes.MAP().getParameterizedType(JavaTypes.STRING(), JavaTypes.OBJECT()), "parameters", NullExpression.instance())),
            TypeSystem.getByFullName("tosa.api.QueryResult").getParameterizedType(getDBType()),
            Modifiers.PublicStatic,
            "TODO",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object context, Object... params) {
                return getDelegate().selectWhere(getDBType(), (String) params[0], (Map<String, Object>) params[1]);
              }
            });

    createMethod(
            "selectLike",
            params(param("template", getDBType(), "the template to use in forming the WHERE clause for the query")),
            TypeSystem.getByFullName("tosa.api.QueryResult").getParameterizedType(getDBType()),
            Modifiers.PublicStatic,
            "TODO",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object context, Object... params) {
                return getDelegate().selectLike(getDBType(), (IDBObject) params[0]);
              }
            });

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
            return getDelegate().newInstance(getDBType(), null);
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
}
