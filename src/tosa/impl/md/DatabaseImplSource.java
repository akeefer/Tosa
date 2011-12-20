package tosa.impl.md;

import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.concurrent.LockingLazyVar;
import tosa.api.IDatabase;
import tosa.dbmd.DatabaseImpl;
import tosa.loader.data.DBData;
import tosa.loader.data.IDBDataSource;
import tosa.loader.parser.DDLDBDataSource;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/28/11
 * Time: 9:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseImplSource {

  // TODO - AHK - Maybe rename this to TosaMetadataService?

  private Map<IModule, Collection<? extends IDatabase>> _dbsByModule;
  private Map<String, IDatabase> _dbsByName;

  private static final LockingLazyVar<DatabaseImplSource> INSTANCE = new LockingLazyVar<DatabaseImplSource>() {
    @Override
    protected DatabaseImplSource init() {
      return new DatabaseImplSource();
    }
  };

  public static DatabaseImplSource getInstance() {
    return INSTANCE.get();
  }

  public DatabaseImplSource() {
    loadDBData();
  }

  public IDatabase getDatabase(String namespace) {
    return _dbsByName.get(namespace);
  }

  public Collection<? extends IDatabase> getAllDatabases() {
    return _dbsByName.values();
  }

  public Collection<? extends IDatabase> getAllDatabasesForModule(IModule module) {
    return _dbsByModule.get(module);
  }

  public void clear() {
    INSTANCE.clear();
  }

  private void loadDBData() {
    _dbsByModule = new HashMap<IModule, Collection<? extends IDatabase>>();
    _dbsByName = new HashMap<String, IDatabase>();

    // TODO - AHK - What if the type system isn't initialized?
    List<? extends IModule> modules = TypeSystem.getExecutionEnvironment().getModules();
    for (IModule module : modules) {
      // TODO - AHK - This seems a bit ugly
      List<IDatabase> dbs = new ArrayList<IDatabase>();
      IDBDataSource dataSource = new DDLDBDataSource();
      Map<String, DBData> dbDataMap = dataSource.getDBData(module);
      for (Map.Entry<String, DBData> dbDataEntry : dbDataMap.entrySet()) {
        IDatabase database = new DatabaseImpl(dbDataEntry.getKey(), dbDataEntry.getValue(), module);
        // TODO - AHK - Validate things!
        dbs.add(database);
        _dbsByName.put(database.getNamespace(), database);
      }
      _dbsByModule.put(module, dbs);
    }
  }
}
