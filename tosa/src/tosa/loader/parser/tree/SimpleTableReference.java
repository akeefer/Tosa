package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class SimpleTableReference extends SQLParsedElement {
  private Token _name;

  public SimpleTableReference(Token t) {
    super(t);
    _name = t;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append("\"").append(_name.getValue()).append("\"");
  }

  public Token getName() {
    return _name;
  }
}
