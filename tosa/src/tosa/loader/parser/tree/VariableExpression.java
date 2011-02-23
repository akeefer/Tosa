package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class VariableExpression extends SQLParsedElement {
  private Token _name;

  public VariableExpression(Token name) {
    super(name);
    _name = name;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append("?");
  }

  public String getName() {
    return _name.getValue();
  }
}
