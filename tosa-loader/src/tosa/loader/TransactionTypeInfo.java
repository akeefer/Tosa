package tosa.loader;

import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.JavaTypes;
import tosa.api.IDBConnection;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionTypeInfo extends TosaBaseTypeInfo {

  private ThreadLocal<Lock> _lock = new ThreadLocal<Lock>();

  public TransactionTypeInfo(ITransactionType type) {
    super(type);

    createMethod("commit", params(), JavaTypes.pVOID(), Modifiers.PublicStatic,
        "Commits the underlying transaction.",
        new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            commitTransaction();
            return null;
          }
        });

    createProperty("Lock", TypeSystem.get(Lock.class), Modifiers.PublicStatic, Writeable.ReadOnly,
        "The thread-local Lock for this Transaction, suitable for use as the argument to a using statement.",
        new IPropertyAccessor() {
          @Override
          public void setValue(Object ctx, Object value) {
          }

          @Override
          public Object getValue(Object ctx) {
            return getLock();
          }
        });

    lockDataStructures();
  }

  private void commitTransaction() {
    // TODO - AHK - I'm not sure we want to swallow exceptions here
    try {
      getConnection().commitTransaction();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private Lock getLock() {
    if (_lock.get() == null) {
      _lock.set(new Lock());
    }
    return _lock.get();
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
    return ((ITransactionType) getOwnersType()).getDatabase().getConnection();
  }

}

