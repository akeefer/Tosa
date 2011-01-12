package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class ColumnSelectList extends SQLParsedElement {
  private Token _name;

  public ColumnSelectList(Token t) {
    super(t);
    _name = t;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append(_name.getValue());
  }
}
