package tosa.loader;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.fs.IResource;
import gw.lang.reflect.IExtendedTypeLoader;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaClassInfo;
import gw.lang.reflect.module.IExecutionEnvironment;
import gw.lang.reflect.module.IModule;
import gw.util.GosuClassUtil;
import gw.util.concurrent.LazyVar;
import tosa.CachedDBObject;
import tosa.loader.data.DBData;
import tosa.loader.data.IDBDataSource;
import tosa.loader.parser.DDLDBDataSource;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBTypeLoader implements IExtendedTypeLoader {

  private IModule _module;
//  private Set<String> _initializedDrivers = new HashSet<String>();
//  private Map<String, DBConnection> _connInfos = new HashMap<String, DBConnection>();

  private LazyVar<Map<String, DBTypeData>> _typeDataByNamespace = new LazyVar<Map<String, DBTypeData>>() {
    protected Map<String, DBTypeData> init() { return initializeDBTypeData(); }
  };

  private LazyVar<Map<String, SQLTypeData>> _sqlTypeDataByName = new LazyVar<Map<String, SQLTypeData>>() {
    protected Map<String, SQLTypeData> init() { return initializeSQLTypeData(); }
  };

  private LazyVar<Set<String>> _namespaces = new LazyVar<Set<String>>() {
    protected Set<String> init() { return initializeNamespaces(); }
  };

  private LazyVar<Set<String>> _typeNames = new LazyVar<Set<String>>() {
    protected Set<String> init() { return initializeTypeNames(); }
  };

  public DBTypeLoader() {
    this(TypeSystem.getExecutionEnvironment(), new HashMap<String, String>());
  }

  // TODO - AHK - This should take a module and a moduleresourceaccess instead
  public DBTypeLoader(IExecutionEnvironment env, Map<String, String> args) {
    _module = env.getCurrentModule();
  }

  @Override
  public IModule getModule() {
    return _module;
  }

  @Override
  public IType getIntrinsicType(Class javaClass) {
    return null;
  }

  @Override
  public IType getIntrinsicType(IJavaClassInfo javaClassInfo) {
    return null;
  }

  @Override
  public IType getType(String fullyQualifiedName) {
    int lastDot = fullyQualifiedName.lastIndexOf('.');
    if (lastDot == -1) {
      return null;
    }
    // TODO - AHK - What do we do if there's a table named "Transaction"?
    // TODO - AHK - Is it really our job to do any caching at all?
    String namespace = fullyQualifiedName.substring(0, lastDot);
    String relativeName = fullyQualifiedName.substring(lastDot + 1);
    DBTypeData dbTypeData = _typeDataByNamespace.get().get(namespace);
    if (dbTypeData == null) {
      SQLTypeData data = _sqlTypeDataByName.get().get(fullyQualifiedName);
      if (data != null) {
        return new SQLType(data, this);
      } else {
        return null;
      }
    }

    if ("Transaction".equals(relativeName)) {
      return new TransactionType(dbTypeData, this);
    } else {
      TableTypeData tableTypeData = dbTypeData.getTable(relativeName);
      if (tableTypeData == null) {
        return null;
      } else {
        return new DBType(this, tableTypeData);
      }
    }
  }

  @Override
  public Set<? extends CharSequence> getAllTypeNames() {
    return _typeNames.get();
  }

  @Override
  public Set<? extends CharSequence> getAllNamespaces() {
    return _namespaces.get();
  }

  @Override
  public URL getResource(String name) {
    return _module.getResource(name);
  }

  @Override
  public File getResourceFile(String name) {
    return TypeSystem.getResourceFileResolver().resolveToFile(name);
  }

  @Override
  public void refresh(boolean clearCachedTypes) {
    // TODO?
  }

  @Override
  public boolean isCaseSensitive() {
    return true;
  }

  @Override
  public List<String> getHandledPrefixes() {
    return Collections.emptyList();
  }

  @Override
  public boolean isNamespaceOfTypeHandled(String fullyQualifiedTypeName) {
    int lastDot = fullyQualifiedTypeName.lastIndexOf('.');
    if (lastDot == -1) {
      return false;
    }
    return _typeDataByNamespace.get().keySet().contains(fullyQualifiedTypeName.substring(0, lastDot));
  }

  @Override
  public List<Throwable> getInitializationErrors() {
    return Collections.emptyList();
  }

  @Override
  public IType getIntrinsicTypeFromObject(Object object) {
    // TODO - AHK - This probably needs to work for the Transaction object as well
    if (object instanceof CachedDBObject) {
      CachedDBObject dbObj = (CachedDBObject) object;
      return dbObj.getIntrinsicType();
    } else {
      return null;
    }
  }

  private Map<String, DBTypeData> initializeDBTypeData() {
    IDBDataSource dataSource = new DDLDBDataSource();
    Map<String, DBData> dbDataMap = dataSource.getDBData(_module);
    Map<String, DBTypeData> dbTypeDataMap = new HashMap<String, DBTypeData>();
    for (Map.Entry<String, DBData> dbDataEntry : dbDataMap.entrySet()) {
      dbTypeDataMap.put(dbDataEntry.getKey(), new DBTypeData(dbDataEntry.getKey(), dbDataEntry.getValue(), this));
    }
    return dbTypeDataMap;
  }


  private Map<String, SQLTypeData> initializeSQLTypeData() {
    Map<String, SQLTypeData> typeData = new HashMap<String, SQLTypeData>();
    for (DBTypeData dbTypeData : _typeDataByNamespace.get().values()) {
      DBData data = dbTypeData.getDBData();
      IFile ddl = data.getDDLFile();
      populateTypeData(dbTypeData,
        GosuClassUtil.getPackage(
          GosuClassUtil.getPackage(dbTypeData.getNamespace())),
        ddl.getParent(),
        typeData);
    }
    return typeData;
  }

  private void populateTypeData(DBTypeData dbTypeData, String namespace, IResource res, Map<String, SQLTypeData> types) {
    if (res instanceof IFile && res.getName().endsWith(".sql")) {
      String name = namespace + "." + ((IFile) res).getBaseName();
      SQLTypeData data = new SQLTypeData(name, dbTypeData, (IFile) res);
      types.put(name, data);
    } else if (res instanceof IDirectory) {
      String nextNamespace = res.getName();
      if (!namespace.equals("")) {
        nextNamespace = namespace + "." + nextNamespace;
      }
      for (IDirectory directory : ((IDirectory) res).listDirs()) {
        populateTypeData(dbTypeData, nextNamespace, directory, types);
      }
      for (IFile file : ((IDirectory) res).listFiles()) {
        populateTypeData(dbTypeData, nextNamespace, file, types);
      }
    }
  }

  private Set<String> initializeNamespaces() {
    Set<String> allNamespaces = new HashSet<String>();
    for (String namespace : _typeDataByNamespace.get().keySet()) {
      addNamespaces(allNamespaces, namespace);
    }
    for (SQLTypeData data : _sqlTypeDataByName.get().values()) {
      addNamespaces(allNamespaces, GosuClassUtil.getPackage(data.getTypeName()));
    }
    return allNamespaces;
  }

  private void addNamespaces(Set<String> allNamespaces, String namespace) {
    String[] nsComponentsArr = namespace.split("\\.");
    for (int i = 0; i < nsComponentsArr.length; i++) {
      String nsName = "";
      for (int n = 0; n < i + 1; n++) {
        if (n > 0) {
          nsName += ".";
        }
        nsName += nsComponentsArr[n];
      }
      allNamespaces.add(nsName);
    }
  }

  private Set<String> initializeTypeNames() {
    Set<String> typeNames = new HashSet<String>();

    for (DBTypeData dbTypeData : _typeDataByNamespace.get().values()) {
      typeNames.add(dbTypeData.getNamespace() + "." + TransactionType.TYPE_NAME);
      typeNames.addAll(dbTypeData.getTypeNames());
    }
    for (SQLTypeData data : _sqlTypeDataByName.get().values()) {
      typeNames.add(data.getTypeName());
    }

    return typeNames;
  }

  // TODO - AHK - This doesn't really belong here, but for now . . .
  public DBTypeData getTypeDataForNamespace(String namespace) {
    return _typeDataByNamespace.get().get(namespace);
  }

}
