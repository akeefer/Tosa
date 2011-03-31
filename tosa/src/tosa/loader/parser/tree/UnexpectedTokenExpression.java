package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class UnexpectedTokenExpression extends SQLParsedElement {
  private Token _token;

  public UnexpectedTokenExpression(Token start) {
    super(start);
    _token = start;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append(" ");
    sb.append(_token.getValue());
    sb.append(" ");
  }
}
