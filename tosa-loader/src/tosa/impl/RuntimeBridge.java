package tosa.impl;

import gw.lang.reflect.ReflectUtil;
import gw.lang.reflect.module.IModule;
import tosa.api.IDBConnection;
import tosa.api.IDBExecutionKernel;
import tosa.api.IDBObject;
import tosa.api.IDBUpgrader;
import tosa.api.IDatabase;
import tosa.loader.DBTypeInfoDelegate;
import tosa.loader.IDBType;

/**
 * Created by IntelliJ IDEA.
 * User: akeefer
 * Date: 2/9/12
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class RuntimeBridge {

  public static IDBConnection createConnection(String jdbcUrl, IModule module) {
    return ReflectUtil.construct("tosa.DBConnection", jdbcUrl, module);  
  }

  // TODO - AHK - Ideally we'd completely kill the need for this in tosa-loader
  public static IDBObject createDBObject(IDBType type, boolean isNew) {
    return ReflectUtil.construct("tosa.CachedDBObject", type, isNew);
  }

  public static DBTypeInfoDelegate createTypeInfoDelegate() {
    return ReflectUtil.construct("tosa.impl.loader.DBTypeInfoDelegateImpl");
  }
  
  public static IDBExecutionKernel createExecutionKernel(IDatabase database) {
    return ReflectUtil.construct("tosa.db.execution.DBExecutionKernelImpl", database);
  }

  public static IDBUpgrader createUpgrader(IDatabase database) {
    return ReflectUtil.construct("tosa.db.execution.DBUpgraderImpl", database);
  }
}
