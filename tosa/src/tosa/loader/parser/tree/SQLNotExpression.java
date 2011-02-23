package tosa.loader.parser.tree;

import tosa.loader.parser.Token;

public class SQLNotExpression extends SQLParsedElement {
  private SQLParsedElement _rhs;

  public SQLNotExpression(Token start, SQLParsedElement rhs) {
    super(start, rhs.lastToken(), rhs);
    _rhs = rhs;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb) {
    sb.append("NOT ");
    _rhs.toSQL(prettyPrint, indent, sb);
  }
}
