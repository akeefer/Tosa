package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

import java.util.Map;

public class SQLParenthesizedExpression extends SQLParsedElement {
  private SQLParsedElement _expr;

  public SQLParenthesizedExpression(Token openParen, SQLParsedElement expr, Token closeParen) {
    super(openParen, expr, closeParen);
    _expr = expr;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    sb.append("(");
    _expr.toSQL(prettyPrint, indent, sb, values);
    sb.append(")");
  }
}
