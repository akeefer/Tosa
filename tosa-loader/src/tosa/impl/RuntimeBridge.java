package tosa.impl;

import gw.lang.reflect.ReflectUtil;
import gw.lang.reflect.module.IModule;
import tosa.api.IDBConnection;
import tosa.api.IDBObject;
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
}
