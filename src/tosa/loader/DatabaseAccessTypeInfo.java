package tosa.loader;

import gw.internal.gosu.runtime.GosuRuntimeMethods;
import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaArrayType;
import gw.lang.reflect.java.IJavaType;
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

    // All the static methods on this type are actually found on the DatabaseAccessTypeDelegate class
    delegateStaticMethods(TypeSystem.getByFullName("tosa.loader.DatabaseAccessTypeDelegate"));

    lockDataStructures();
  }

  @Override
  protected Object getFirstArgForDelegatedMethods() {
    return ((IDatabaseAccessType) getOwnersType()).getDatabaseInstance();
  }
}

