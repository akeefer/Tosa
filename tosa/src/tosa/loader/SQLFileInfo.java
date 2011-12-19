package tosa.loader;

import gw.fs.IFile;
import gw.util.GosuExceptionUtil;
import gw.util.StreamUtil;
import gw.util.concurrent.LockingLazyVar;
import tosa.dbmd.DatabaseImpl;
import tosa.loader.parser.QueryParser;
import tosa.loader.parser.Token;
import tosa.loader.parser.tree.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class SQLFileInfo {

  private DatabaseImpl _db;
  private IFile _sql;
  private String _name;
  private LockingLazyVar<SelectStatement> _select;

  public SQLFileInfo(String name, DatabaseImpl dbData, final IFile sql) {
    _name = name;
    _sql = sql;
    _db = dbData;
    _select = new LockingLazyVar<SelectStatement>() {
      @Override
      protected SelectStatement init() {
        try {
          InputStream is = _sql.openInputStream();
          String content = new String(StreamUtil.getContent(is));
          is.close();
          QueryParser parser = new QueryParser(Token.tokenize(content), _db.getDBData());
          return parser.parseTopLevelSelect();
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

  public String getSQL(HashMap<String, Object> values) {
    return _select.get().toSQL(values);
  }

  public SelectStatement getSelect() {
    return _select.get();
  }

  public List<SQLParameterInfo> getParameterInfos() {
    return _select.get().getParameters();
  }

  public List<VariableExpression> getVariables() {
    return _select.get().getVariables();
  }

  public String getFileName() {
    return _sql.getName();
  }
}
