package tosa.loader.parser.tree;

import tosa.loader.data.DBData;
import tosa.loader.data.TableData;
import tosa.loader.parser.Token;

import java.util.Map;

public class SimpleTableReference extends SQLParsedElement {
  private Token _name;
  private TableData _table;

  public SimpleTableReference(Token t) {
    super(t);
    _name = t;
  }

  @Override
  public void verify(DBData dbData) {
    _table = dbData.getTable(_name.getValue());
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(_table.getPossiblyQuotedName());
  }

  public Token getName() {
    return _name;
  }

  public TableData getTableData() {
    return _table;
  }
}
