package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class CountAllExpression extends SQLParsedElement {
  public CountAllExpression(Token start, Token end) {
    super(start, end);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append("COUNT(*)");
  }
}
