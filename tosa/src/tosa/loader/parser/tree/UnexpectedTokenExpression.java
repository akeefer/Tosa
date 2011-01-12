package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class UnexpectedTokenExpression extends SQLParsedElement {
  private Token _token;

  public UnexpectedTokenExpression(Token start) {
    super(start);
    _token = start;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append(" ");
    sb.append(_token.getValue());
    sb.append(" ");
  }
}
