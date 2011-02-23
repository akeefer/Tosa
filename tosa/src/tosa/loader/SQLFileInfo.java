package tosa.loader;

import gw.fs.IFile;
import gw.util.GosuExceptionUtil;
import gw.util.StreamUtil;
import gw.util.concurrent.LazyVar;
import tosa.api.IDatabase;
import tosa.dbmd.DatabaseImpl;
import tosa.loader.data.DBData;
import tosa.loader.parser.mysql.MySQL51SQLParser;
import tosa.loader.parser.tree.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SQLFileInfo {

  private DatabaseImpl _db;
  private IFile _sql;
  private String _name;
  private LazyVar<SelectStatement> _select;

  public SQLFileInfo(String name, DatabaseImpl dbData, IFile sql) {
    _name = name;
    _sql = sql;
    _db = dbData;
    _select = new LazyVar<SelectStatement>() {
      @Override
      protected SelectStatement init() {
        try {
          FileReader reader = new FileReader(_sql.toJavaFile());
          String content = StreamUtil.getContent(reader);
          reader.close();
          MySQL51SQLParser parser = new MySQL51SQLParser();
          return parser.parseSQLFile(_db.getDBData(), content);
        } catch (IOException e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
    };
  }

  public String getTypeName() {
    return _name;
  }

  public DatabaseImpl getDatabase() {
    return _db;
  }

  public String getSQL() {
    return _select.get().toSQL();
  }

  public SelectStatement getSelect() {
    return _select.get();
  }

  public List<SQLParameterInfo> getParameterInfos() {
    return _select.get().getParameters();
  }
}
