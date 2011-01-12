package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class SQLAdditiveExpression extends SQLParsedElement{
  private SQLParsedElement _lhs;
  private Token _op;
  private SQLParsedElement _rhs;

  public SQLAdditiveExpression(SQLParsedElement lhs, Token op, SQLParsedElement rhs) {
    super(lhs.firstToken(), rhs.lastToken(), lhs, rhs);
    _lhs = lhs;
    _op = op;
    _rhs = rhs;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    _lhs.toSQL(prettyPrint, indent, sb);
    sb.append(" ");
    sb.append(_op.getValue());
    sb.append(" ");
    _rhs.toSQL(prettyPrint, indent, sb);
  }
}
