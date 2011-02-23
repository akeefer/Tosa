package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class SimpleTableReference extends SQLParsedElement {
  private Token _name;

  public SimpleTableReference(Token t) {
    super(t);
    _name = t;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append("\"").append(_name.getValue()).append("\"");
  }

  public Token getName() {
    return _name;
  }
}
