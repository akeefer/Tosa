package tosa.loader.parser.tree;

import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.Map;

public class DerivedColumn extends SQLParsedElement {

  private SQLParsedElement _value;
  private Token _colName;

  public DerivedColumn(SQLParsedElement value, Token colName) {
    super(value, colName);
    _value = value;
    _colName = colName;
  }

  @Override
  public void resolveTypes(DBData dbData) {
    super.resolveTypes(dbData);
    setType(_value.getDBType());
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    _value.toSQL(prettyPrint, indent, sb, values);
    sb.append(" AS ");
    sb.append(_colName.getValue());
  }

  public String getName() {
    return _colName.getValue();
  }
}
