package tosa.loader;

import gw.internal.gosu.runtime.GosuRuntimeMethods;
import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaArrayType;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.java.JavaTypes;
import tosa.api.IDatabase;
import tosa.dbmd.DatabaseImpl;

import java.awt.geom.IllegalPathStateException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseAccessTypeInfo extends TosaBaseTypeInfo {

  // TODO - AHK - Something around recreating tables and the like
  // TODO - AHK - An "instance" property to get a hold of the underlying IDatabase

  public DatabaseAccessTypeInfo(IDatabaseAccessType type) {
    super(type);

    createProperty(
            "JdbcUrl",
            JavaTypes.STRING(),
            Modifiers.PublicStatic,
            Writeable.ReadWrite,
            "The jdbc url for this database",
            new IPropertyAccessor() {
              @Override
              public Object getValue(Object o) {
                return getDb().getJdbcUrl();
              }

              @Override
              public void setValue(Object o, Object o1) {
                getDb().setJdbcUrl((String) o1);
              }
            });
    
    createMethod(
            "createTables",
            params(),
            JavaTypes.pVOID(),
            Modifiers.PublicStatic,
            "Creates the tables for this database by executing the DDL statements.",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object o, Object... objects) {
                getDb().createTables();
                return null;
              }
            }
    );

    createMethod(
            "dropTables",
            params(),
            JavaTypes.pVOID(),
            Modifiers.PublicStatic,
            "Drops all tables in this database.",
            new IMethodCallHandler() {
              @Override
              public Object handleCall(Object o, Object... objects) {
                getDb().dropTables();
                return null;
              }
            }
    );

    lockDataStructures();
  }

  private IDatabase getDb() {
    return ((IDatabaseAccessType) getOwnersType()).getDatabaseInstance();
  }
}

