package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class SQLSignedExpression extends SQLParsedElement{
  private Token _op;
  private SQLParsedElement _rhs;

  public SQLSignedExpression(Token op, SQLParsedElement rhs) {
    super(op, rhs.lastToken(), rhs);
    _op = op;
    _rhs = rhs;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append(_op.getValue());
    _rhs.toSQL(prettyPrint, indent, sb);
  }
}
