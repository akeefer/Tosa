package tosa.loader;

import gw.fs.IFile;
import gw.lang.reflect.IExtendedTypeLoader;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeRef;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaClassInfo;
import gw.lang.reflect.module.IExecutionEnvironment;
import gw.lang.reflect.module.IModule;
import gw.util.Pair;
import gw.util.concurrent.LazyVar;
import tosa.CachedDBObject;
import tosa.DBConnection;
import tosa.loader.data.DBData;
import tosa.loader.data.IDBDataSource;
import tosa.loader.parser.DDLDBDataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

//  private Set<String> getAllFullNamespaces() {
//    Set<String> allFullNamespaces = new HashSet<String>();
//    for (Pair<String, IFile> dbcFile : _module.getResourceAccess().findAllFilesByExtension(".dbc")) {
//      String fileName = dbcFile.getFirst();
//      allFullNamespaces.add(fileName.substring(0, fileName.length() - ".dbc".length()).replace("/", "."));
//    }
//    return allFullNamespaces;
//  }
//
//  private DBConnection getConnInfo(String namespace) throws IOException {
//    DBConnection connInfo = _connInfos.get(namespace);
//    if (connInfo == null && getAllFullNamespaces().contains(namespace)) {
//      URL connRsrc = _module.getResource(namespace.replace('.', '/') + ".dbc");
//      InputStream connRsrcStream = connRsrc.openStream();
//      StringBuilder connUrlBuilder = new StringBuilder();
//      String line;
//      BufferedReader reader = new BufferedReader(new InputStreamReader(connRsrcStream));
//      while ((line = reader.readLine()) != null) {
//        connUrlBuilder.append(line);
//      }
//      String connUrl = connUrlBuilder.toString();
//      connInfo = new DBConnection(connUrl, namespace, this);
//      _connInfos.put(namespace, connInfo);
//    }
//    return connInfo;
//  }

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
      return null;
    }

    if ("Transaction".equals(relativeName)) {
      return new TransactionType(dbTypeData, this);
    } else {
      TableTypeData tableTypeData = dbTypeData.getTable(relativeName);
      if (tableTypeData == null) {
        return null;
      } else {
        return new DBType(relativeName, this, null, tableTypeData);
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
    if (object instanceof CachedDBObject) {
      CachedDBObject dbObj = (CachedDBObject) object;
      return getType(dbObj.getConnection().getNamespace() + "." + dbObj.getTableName());
    } else {
      return null;
    }
  }

  private Map<String, DBTypeData> initializeDBTypeData() {
    IDBDataSource dataSource = new DDLDBDataSource();
    Map<String, DBData> dbDataMap = dataSource.getDBData(_module);
    Map<String, DBTypeData> dbTypeDataMap = new HashMap<String, DBTypeData>();
    for (Map.Entry<String, DBData> dbDataEntry : dbDataMap.entrySet()) {
      dbTypeDataMap.put(dbDataEntry.getKey(), new DBTypeData(dbDataEntry.getKey(), dbDataEntry.getValue()));
    }
    return dbTypeDataMap;
  }

  private Set<String> initializeNamespaces() {
    Set<String> allNamespaces = new HashSet<String>();
    for (String namespace : _typeDataByNamespace.get().keySet()) {
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
    return allNamespaces;
  }

  private Set<String> initializeTypeNames() {
    Set<String> typeNames = new HashSet<String>();

    for (DBTypeData dbTypeData : _typeDataByNamespace.get().values()) {
      typeNames.add(dbTypeData.getNamespace() + "." + TransactionType.TYPE_NAME);
      typeNames.addAll(dbTypeData.getTypeNames());
    }

    return typeNames;
  }

}
