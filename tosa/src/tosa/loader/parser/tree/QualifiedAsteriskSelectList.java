package tosa.loader.parser.tree;

import sun.tools.tree.ReturnStatement;
import tosa.loader.parser.Token;

import java.util.Map;

public class QualifiedAsteriskSelectList extends SQLParsedElement {
  public QualifiedAsteriskSelectList(Token start, Token end) {
    super(start,  end);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append("\"");
    sb.append(getFirst().toString());
    sb.append("\"");
    sb.append(".*");
  }

  public String getTableName() {
    return getFirst().toString();
  }
}
