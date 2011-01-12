package tosa.loader;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.util.GosuExceptionUtil;
import gw.util.StreamUtil;
import gw.util.concurrent.LazyVar;
import tosa.loader.parser.mysql.MySQL51SQLParser;
import tosa.loader.parser.tree.SelectStatement;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SQLTypeData {

  private DBTypeData _dbTypeData;
  private IFile _sql;
  private String _name;
  private LazyVar<SelectStatement> _select;

  public SQLTypeData(String name, DBTypeData dbTypeData, IFile sql) {
    _name = name;
    _sql = sql;
    _dbTypeData = dbTypeData;
    _select = new LazyVar<SelectStatement>() {
      @Override
      protected SelectStatement init() {
        try {
          FileReader reader = new FileReader(_sql.toJavaFile());
          String content = StreamUtil.getContent(reader);
          reader.close();
          MySQL51SQLParser parser = new MySQL51SQLParser();
          return parser.parseSQLFile(_dbTypeData, content);
        } catch (IOException e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
    };
  }

  public String getTheOneTrueMethodName() {
    return "select"; //TODO cgross "insert", "update", "execute"
  }

  public String getTypeName() {
    return _name;
  }

  public DBTypeData getDBTypeData() {
    return _dbTypeData;
  }

  public String getSQL() {
    return _select.get().toSQL();
  }

  public IType getResultType() {
    return _select.get().getSelectType();
  }

  public List<SQLParameterInfo> getParameterInfos() {
    return _select.get().getParameters();
  }
}
