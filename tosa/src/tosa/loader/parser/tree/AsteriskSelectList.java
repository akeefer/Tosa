package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class AsteriskSelectList extends SQLParsedElement {
  public AsteriskSelectList(Token t) {
    super(t);
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append("*");
  }
}
