package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class AsteriskSelectList extends SQLParsedElement {
  public AsteriskSelectList(Token t) {
    super(t);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append("*");
  }
}
