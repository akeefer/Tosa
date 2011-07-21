package tosa.loader;

import gw.lang.reflect.BaseTypeInfo;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.PropertyInfoBuilder;
import gw.lang.reflect.TypeSystem;
import tosa.ConnectionWrapper;
import tosa.DBConnection;
import tosa.api.IDBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionTypeInfo extends BaseTypeInfo {

  private IMethodInfo _commitMethod;
  private IPropertyInfo _lockProperty;
  private ThreadLocal<Lock> _lock = new ThreadLocal<Lock>();

  public TransactionTypeInfo(TransactionType type) {
    super(type);
    _commitMethod = new MethodInfoBuilder().withName("commit").withStatic()
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            // TODO - AHK - I'm not sure we want to swallow exceptions here
            try {
              getConnection().commitTransaction();
            } catch (SQLException e) {
              e.printStackTrace();
            }
            return null;
          }
        }).build(this);
    _lockProperty = new PropertyInfoBuilder().withName("Lock").withStatic()
        .withWritable(false).withType(TypeSystem.get(Lock.class))
        .withAccessor(new IPropertyAccessor() {
          @Override
          public void setValue(Object ctx, Object value) {
          }

          @Override
          public Object getValue(Object ctx) {
            if (_lock.get() == null) {
              _lock.set(new Lock());
            }
            return _lock.get();
          }
        }).build(this);
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return Arrays.asList(_commitMethod);
  }

  @Override
  public IMethodInfo getMethod(CharSequence methodName, IType... params) {
    if (params == null || params.length == 0) {
      if ("commit".equals(methodName)) {
        return _commitMethod;
      }
    }
    return null;
  }

  @Override
  public IMethodInfo getCallableMethod(CharSequence method, IType... params) {
    return getMethod(method, params);
  }

  @Override
  public List<? extends IPropertyInfo> getProperties() {
    return Collections.singletonList(_lockProperty);
  }

  @Override
  public IPropertyInfo getProperty(CharSequence propName) {
    if (propName.equals("Lock")) {
      return _lockProperty;
    }
    return null;
  }

  @Override
  public CharSequence getRealPropertyName(CharSequence propName) {
    return propName;
  }

  public class Lock {
    public void lock() {
      // TODO - AHK - I'm not sure we want to swallow exceptions here
      try {
        getConnection().startTransaction();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    public void unlock() {
      // TODO - AHK - I'm not sure we want to swallow exceptions here
      try {
        getConnection().endTransaction();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private IDBConnection getConnection() {
    return ((TransactionType) getOwnersType()).getDatabaseImpl().getConnection();
  }

}

