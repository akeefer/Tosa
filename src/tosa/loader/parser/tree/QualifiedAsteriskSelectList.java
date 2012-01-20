package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class QualifiedAsteriskSelectList  extends SQLParsedElement {

  private Token _tableName;

  public QualifiedAsteriskSelectList(Token start, Token end) {
    super(start, end);
    _tableName = start;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(_tableName.getValue());
    sb.append(".*");
  }

  public String getTableName() {
    return _tableName.getValue();
  }
}
