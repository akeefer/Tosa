package tosa.loader;

import gw.fs.IFile;
import gw.lang.reflect.IExtendedTypeLoader;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaClassInfo;
import gw.lang.reflect.module.IExecutionEnvironment;
import gw.lang.reflect.module.IModule;
import gw.util.GosuClassUtil;
import gw.util.Pair;
import gw.util.concurrent.LockingLazyVar;
import tosa.CachedDBObject;
import tosa.api.IDBTable;
import tosa.api.IDatabase;
import tosa.dbmd.DBTableImpl;
import tosa.dbmd.DatabaseImpl;
import tosa.impl.md.DatabaseImplSource;
import tosa.loader.data.DBData;
import tosa.loader.data.IDBDataSource;
import tosa.loader.parser.DDLDBDataSource;

import java.io.File;
import java.net.URL;
import java.util.*;

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

  private LockingLazyVar<Map<String, DatabaseImpl>> _typeDataByNamespace = new LockingLazyVar<Map<String, DatabaseImpl>>() {
    protected Map<String, DatabaseImpl> init() { return initializeDBTypeData(); }
  };

  private LockingLazyVar<Map<String, SQLFileInfo>> _sqlFilesByName = new LockingLazyVar<Map<String, SQLFileInfo>>() {
    protected Map<String, SQLFileInfo> init() { return initializeSQLFiles(); }
  };

  private LockingLazyVar<Set<String>> _namespaces = new LockingLazyVar<Set<String>>() {
    protected Set<String> init() { return initializeNamespaces(); }
  };

  private LockingLazyVar<Set<String>> _typeNames = new LockingLazyVar<Set<String>>() {
    protected Set<String> init() { return initializeTypeNames(); }
  };

  private Map<String, List<String>> _typeNamesByFile = new HashMap<String, List<String>>();

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
  public IType getType(String fullyQualifiedName) {
    int lastDot = fullyQualifiedName.lastIndexOf('.');
    if (lastDot == -1) {
      return null;
    }
    // TODO - AHK - What do we do if there's a table named "Transaction"?
    // TODO - AHK - Is it really our job to do any caching at all?
    String namespace = fullyQualifiedName.substring(0, lastDot);
    String relativeName = fullyQualifiedName.substring(lastDot + 1);
    DatabaseImpl databaseImpl = _typeDataByNamespace.get().get(namespace);
    if (databaseImpl == null) {
      SQLFileInfo data = _sqlFilesByName.get().get(fullyQualifiedName);
      if (data != null) {
        SQLType sqlType = new SQLType(data, this);
        addTypeForFile(data.getSqlFile(), sqlType);
        return sqlType.getTypeReference();
      } else {
        return null;
      }
    }

    IType rVal = null;
    if (TransactionType.TYPE_NAME.equals(relativeName)) {
      // TODO - AHK - Turn that into a type reference
      rVal = new TransactionType(databaseImpl, this);
    } else if (DatabaseAccessType.TYPE_NAME.equals(relativeName)) {
      rVal = new DatabaseAccessType(databaseImpl, this);
    } else {
      IDBTable dbTable = databaseImpl.getTable(relativeName);
      if (dbTable != null) {
        rVal = new DBType(this, dbTable).getTypeReference();
      }
    }

    if (rVal != null) {
      addTypeForFile(databaseImpl.getDdlFile(), rVal);
    }
    return rVal;
  }

  private void addTypeForFile(IFile file, IType type) {
    String filePath = file.getPath().getPathString();
    List<String> typeNamesByFile = _typeNamesByFile.get(filePath);
    if (typeNamesByFile == null) {
      typeNamesByFile = new ArrayList<String>();
      _typeNamesByFile.put(filePath, typeNamesByFile);
    }
    typeNamesByFile.add(type.getName());
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
    throw new UnsupportedOperationException();
//    return _module.getResource(name);
  }

  @Override
  public File getResourceFile(String name) {
    return TypeSystem.getResourceFileResolver().resolveToFile(name);
  }

  @Override
  public List<IType> refreshedFile(IFile iFile) {
    List<String> typeNames = _typeNamesByFile.get(iFile.getPath().getPathString());
    // For now, we clear all types on any change to  a .ddl or .sql file; we know the set of types
    // that came from any existing SQL or DDL file, but if it's a new sql or ddl file we still need to clear our
    // caches.  This could be made more sophisticated in the future by detecting if it's a new file and
    // responding appropriately, without having to re-parse all existing files, and to ignore entirely files
    // that aren't within this loader's modules
    boolean clearTypes = (typeNames != null || iFile.getExtension().equals("ddl") || iFile.getExtension().equals("sql"));

    if (clearTypes) {
      DatabaseImplSource.getInstance().clear();
      _typeDataByNamespace.clear();
      _sqlFilesByName.clear();
      _namespaces.clear();
      _typeNames.clear();
      _typeNamesByFile.clear();
    }

    if (typeNames != null) {
      List<IType> reloadedTypes = new ArrayList<IType>();
      for (String typeName : typeNames) {
        reloadedTypes.add(TypeSystem.getByFullName(typeName));
      }
      return reloadedTypes;
    } else {
      return null;
    }
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
  public IType getIntrinsicTypeFromObject(Object object) {
    // TODO - AHK - This probably needs to work for the Transaction object as well
    if (object instanceof CachedDBObject) {
      CachedDBObject dbObj = (CachedDBObject) object;
      return dbObj.getIntrinsicType();
    } else {
      return null;
    }
  }

  private Map<String, DatabaseImpl> initializeDBTypeData() {
    Map<String, DatabaseImpl> dbTypeDataMap = new HashMap<String, DatabaseImpl>();
    for (IDatabase dbImpl : DatabaseImplSource.getInstance().getAllDatabasesForModule(_module)) {
      dbTypeDataMap.put(dbImpl.getNamespace(), (DatabaseImpl) dbImpl);
    }
    return dbTypeDataMap;
  }

  private Map<String, SQLFileInfo> initializeSQLFiles() {
    HashMap<String, SQLFileInfo> results = new HashMap<String, SQLFileInfo>();
    for (Pair<String, IFile> pair : _module.getFileRepository().findAllFilesByExtension(".sql")) {
      String fileName = pair.getFirst();
      IFile sqlFil = pair.getSecond();
      for (DatabaseImpl db : _typeDataByNamespace.get().values()) {
        if (sqlFil.isDescendantOf(db.getDBData().getDdlFile().getParent())) {
          String queryName = fileName.substring(0, fileName.length() - ".sql".length()).replace("/", ".");
          results.put(queryName, new SQLFileInfo(queryName, db, sqlFil));
          break;
        }
      }
    }
    return results;
  }

  private Set<String> initializeNamespaces() {
    Set<String> allNamespaces = new HashSet<String>();
    for (String namespace : _typeDataByNamespace.get().keySet()) {
      splitStringInto(allNamespaces, namespace);
    }
    for (SQLFileInfo sqlFileInfo : _sqlFilesByName.get().values()) {
      splitStringInto(allNamespaces, GosuClassUtil.getPackage(sqlFileInfo.getTypeName()));
    }
    return allNamespaces;
  }

  private void splitStringInto(Set<String> allNamespaces, String namespace) {
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

    for (IDatabase database : _typeDataByNamespace.get().values()) {
      typeNames.add(database.getNamespace() + "." + TransactionType.TYPE_NAME);
      typeNames.add(database.getNamespace() + "." + DatabaseAccessType.TYPE_NAME);
      for (IDBTable table : database.getAllTables()) {
        if (!table.getName().contains("join_")) {
          // TODO - AHK - Should there be a utility method for converting from table to entity name?
          typeNames.add(database.getNamespace() + "." + table.getName());
        }
      }
    }
    for (SQLFileInfo sqlFileInfo : _sqlFilesByName.get().values()) {
      typeNames.add(sqlFileInfo.getTypeName());
    }
    return typeNames;
  }

  @Override
  public boolean handlesNonPrefixLoads() {
    return true;
  }

  @Override
  public boolean isInited() {
    // TODO - AHK
    return true;
  }

  @Override
  public void init() {
    // TODO - AHK
  }

  @Override
  public void uninit() {
    // TODO - AHK
  }

  public List<IType> getTypesForFile(IFile file) {
    return Collections.emptyList();
  }
}
